package com.example.medisync.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- WebRTC Imports ---
import org.webrtc.*

// --- WebSocket Imports ---
import com.example.medisync.networks.VideoSignalEvent
import com.example.medisync.networks.VideoWebSocketManager

class VideoCallViewModel(application: Application) : AndroidViewModel(application) {

    // ─── UI State Flows ───────────────────────────────────────────────────────
    private val _localPreviewTrack = MutableStateFlow<VideoTrack?>(null)
    val localPreviewTrack: StateFlow<VideoTrack?> = _localPreviewTrack.asStateFlow()
    private val _isPeerConnected = MutableStateFlow(false)
    val isPeerConnected: StateFlow<Boolean> = _isPeerConnected.asStateFlow()

    private val _isLocalVideoOn = MutableStateFlow(true)
    val isLocalVideoOn: StateFlow<Boolean> = _isLocalVideoOn.asStateFlow()

    private val _isMicOn = MutableStateFlow(true)
    val isMicOn: StateFlow<Boolean> = _isMicOn.asStateFlow()

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    // ─── WebRTC Core Components ───────────────────────────────────────────────

    val eglBaseContext: EglBase.Context by lazy { EglBase.create().eglBaseContext }
    val localEglBaseContext: EglBase.Context by lazy { EglBase.create().eglBaseContext }  // ADD

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localVideoSource: VideoSource? = null
    private var localAudioSource: AudioSource? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var localAudioTrack: AudioTrack? = null

    // ─── WebSocket Manager ────────────────────────────────────────────────────

    private val wsManager = VideoWebSocketManager()
    private var currentRoomId: Int = -1
    private var remotePeerId: String? = null
    private var localRendererRef: org.webrtc.VideoSink? = null

    init {
        initializeWebRTC(application)
        startLocalVideo(application)
    }

    // ─── Connect to Room ──────────────────────────────────────────────────────

    fun connect(roomId: Int) {
        currentRoomId = roomId
        wsManager.connect(roomId)
        observeSignals()
    }

    // ─── Observe Incoming Signals ─────────────────────────────────────────────

    private fun observeSignals() {
        viewModelScope.launch {
            wsManager.events.collect { event ->
                when (event) {

                    is VideoSignalEvent.RoomJoined -> {
                        Log.d("VideoVM", "Joined room: ${event.roomId} as ${event.socketId}")
                    }

                    is VideoSignalEvent.UserJoined -> {
                        // Someone joined — we are the caller, create offer
                        Log.d("VideoVM", "User joined: ${event.socketId}")
                        remotePeerId = event.socketId
                        createPeerConnection()
                        createOffer(event.socketId)
                    }

                    is VideoSignalEvent.UserLeft -> {
                        Log.d("VideoVM", "User left: ${event.socketId}")
                        _isPeerConnected.value = false
                        _remoteVideoTrack.value = null
                        peerConnection?.close()
                        peerConnection = null
                    }

                    is VideoSignalEvent.OfferReceived -> {
                        // We are the callee — set remote desc then answer
                        Log.d("VideoVM", "Offer received from: ${event.fromId}")
                        remotePeerId = event.fromId
                        createPeerConnection()
                        setRemoteDescription(event.sdp, "offer") {
                            createAnswer(event.fromId)
                        }
                    }

                    is VideoSignalEvent.AnswerReceived -> {
                        Log.d("VideoVM", "Answer received from: ${event.fromId}")
                        setRemoteDescription(event.sdp, "answer") {
                            _isPeerConnected.value = true
                        }
                    }

                    is VideoSignalEvent.IceCandidateReceived -> {
                        Log.d("VideoVM", "ICE candidate received from: ${event.fromId}")
                        addIceCandidate(event.candidate, event.sdpMid, event.sdpMLineIndex)
                    }

                    is VideoSignalEvent.RenegotiateReceived -> {
                        Log.d("VideoVM", "Renegotiate received from: ${event.fromId}")
                        if (peerConnection == null) {
                            Log.w("VideoVM", "Renegotiate received but no peer connection — ignoring")
                            return@collect
                        }
                        setRemoteDescription(event.sdp, "offer") {
                            createAnswer(event.fromId)
                        }
                    }
                }
            }
        }
    }

    // ─── PeerConnection Setup ─────────────────────────────────────────────────
   private var isNegotiating = false

    private fun createPeerConnection() {
        val factory = peerConnectionFactory ?: return

        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                .createIceServer(),
            PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
                .setUsername("openrelayproject")
                .setPassword("openrelayproject")
                .createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {

            override fun onIceCandidate(candidate: IceCandidate) {
                // Send our ICE candidate to the peer via signaling server
                Log.d("VideoVM", "New ICE candidate: ${candidate.sdp}")
                wsManager.sendIceCandidate(
                    candidate = candidate.sdp,
                    sdpMid = candidate.sdpMid,
                    sdpMLineIndex = candidate.sdpMLineIndex,
                    roomId = currentRoomId,
                    targetId = remotePeerId
                )
            }

            override fun onTrack(transceiver: RtpTransceiver) {
                val track = transceiver.receiver.track()
                if (track is VideoTrack) {
                    Log.d("VideoVM", "Remote video track received")
                    _remoteVideoTrack.value = track
                    _isPeerConnected.value = true
                    // Re-attach local preview sink after remote track arrives
                    localRendererRef?.let {
                        _localPreviewTrack.value?.removeSink(it)
                        _localPreviewTrack.value?.addSink(it)
                        Log.d("VideoVM", "Re-attached local preview sink after remote track")
                    }
                }
            }
            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                Log.d("VideoVM", "Connection state: $newState")
                when (newState) {
                    PeerConnection.PeerConnectionState.CONNECTED -> _isPeerConnected.value = true
                    PeerConnection.PeerConnectionState.DISCONNECTED,
                    PeerConnection.PeerConnectionState.FAILED -> {
                        _isPeerConnected.value = false
                        _remoteVideoTrack.value = null
                    }
                    else -> {}
                }
            }

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {}
            override fun onSignalingChange(newState: PeerConnection.SignalingState) {}
            override fun onDataChannel(dataChannel: DataChannel) {}
            override fun onRenegotiationNeeded() {
                // Triggered when tracks change mid-call
                if (!_isPeerConnected.value || isNegotiating) {
                    Log.d("VideoVM", "Renegotiation needed but not connected yet — ignoring")
                    return
                }
                isNegotiating = true
                Log.d("VideoVM", "Renegotiation needed")
                remotePeerId?.let { createRenegotiate(it) }
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
            override fun onAddStream(stream: MediaStream) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {}
        })

        // Add local tracks to the peer connection
        localAudioTrack?.let { peerConnection?.addTrack(it) }
        _localVideoTrack.value?.let { peerConnection?.addTrack(it) }
    }

    // ─── WebRTC Offer / Answer / ICE ──────────────────────────────────────────

    private fun createOffer(targetId: String) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        wsManager.sendOffer(
                            sdp = sdp.description,
                            roomId = currentRoomId,
                            targetId = targetId
                        )
                    }
                    override fun onSetFailure(error: String) { Log.e("VideoVM", "setLocalDesc failed: $error") }
                    override fun onCreateSuccess(sdp: SessionDescription) {}
                    override fun onCreateFailure(error: String) {}
                }, sdp)
            }
            override fun onCreateFailure(error: String) { Log.e("VideoVM", "createOffer failed: $error") }
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, constraints)
    }

    private fun createAnswer(targetId: String) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        isNegotiating = false
                        wsManager.sendAnswer(
                            sdp = sdp.description,
                            roomId = currentRoomId,
                            targetId = targetId
                        )
                    }
                    override fun onSetFailure(error: String) { Log.e("VideoVM", "setLocalDesc failed: $error") }
                    override fun onCreateSuccess(sdp: SessionDescription) {}
                    override fun onCreateFailure(error: String) {}
                }, sdp)
            }
            override fun onCreateFailure(error: String) { Log.e("VideoVM", "createAnswer failed: $error") }
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, constraints)
    }

    private fun setRemoteDescription(sdp: String, type: String, onSuccess: () -> Unit) {
        val sessionDescription = SessionDescription(
            if (type == "offer") SessionDescription.Type.OFFER else SessionDescription.Type.ANSWER,
            sdp
        )

        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d("VideoVM", "Remote description set successfully")
                onSuccess()
            }
            override fun onSetFailure(error: String) { Log.e("VideoVM", "setRemoteDesc failed: $error") }
            override fun onCreateSuccess(sdp: SessionDescription) {}
            override fun onCreateFailure(error: String) {}
        }, sessionDescription)
    }

    private fun addIceCandidate(candidate: String, sdpMid: String, sdpMLineIndex: Int) {
        val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidate)
        peerConnection?.addIceCandidate(iceCandidate)
    }

    private fun createRenegotiate(targetId: String) {
        val constraints = MediaConstraints()

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        wsManager.sendRenegotiate(
                            sdp = sdp.description,
                            roomId = currentRoomId,
                            targetId = targetId
                        )
                    }
                    override fun onSetFailure(error: String) { Log.e("VideoVM", "setLocalDesc renegotiate failed: $error") }
                    override fun onCreateSuccess(sdp: SessionDescription) {}
                    override fun onCreateFailure(error: String) {}
                }, sdp)
            }
            override fun onCreateFailure(error: String) { Log.e("VideoVM", "createRenegotiate failed: $error") }
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, constraints)
    }

    // ─── Local Video Setup (unchanged) ───────────────────────────────────────

    private fun initializeWebRTC(context: Context) {
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()
    }

    private fun startLocalVideo(context: Context) {
        val factory = peerConnectionFactory ?: return

        localAudioSource = factory.createAudioSource(MediaConstraints())
        localAudioTrack = factory.createAudioTrack("local_audio_track", localAudioSource)

        videoCapturer = createCameraCapturer(context)
        localVideoSource = factory.createVideoSource(videoCapturer!!.isScreencast)

        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
        videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource!!.capturerObserver)
        videoCapturer?.startCapture(1280, 720, 30)

        // Track for PeerConnection (sent to remote peer)
        val videoTrack = factory.createVideoTrack("local_video_track", localVideoSource)
        _localVideoTrack.value = videoTrack

        // Separate track for local preview (never added to PeerConnection)
        val previewTrack = factory.createVideoTrack("local_preview_track", localVideoSource)
        _localPreviewTrack.value = previewTrack
        localRendererRef?.let {
            previewTrack.removeSink(it)
            previewTrack.addSink(it)
        }
        Log.d("VideoVM", "Preview track created: $previewTrack, source: $localVideoSource")
    }
    private fun createCameraCapturer(context: Context): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames

        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        return null
    }

    // ─── User Actions (unchanged) ─────────────────────────────────────────────

    fun toggleMic() {
        _isMicOn.value = !_isMicOn.value
        localAudioTrack?.setEnabled(_isMicOn.value)
    }

    fun toggleVideo() {
        _isLocalVideoOn.value = !_isLocalVideoOn.value
        localVideoTrack.value?.setEnabled(_isLocalVideoOn.value)
    }

    fun flipCamera() {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {}
            override fun onCameraSwitchError(errorDescription: String) {}
        })
    }
    fun attachPreviewSink(renderer: org.webrtc.VideoSink) {
        val track = _localPreviewTrack.value
        Log.d("VideoVM", "attachPreviewSink called — track: $track")
        track?.removeSink(renderer)
        track?.addSink(renderer)
    }
    fun setLocalRenderer(renderer: org.webrtc.VideoSink) {
        localRendererRef = renderer
        _localPreviewTrack.value?.removeSink(renderer)
        _localPreviewTrack.value?.addSink(renderer)
        Log.d("VideoVM", "Local renderer set and sink attached")
    }
    // ─── End Call ─────────────────────────────────────────────────────────────

    fun endCall() {
        try { videoCapturer?.stopCapture() } catch (e: Exception) { e.printStackTrace() }

//        localVideoTrack.value?.dispose()
//        localAudioTrack?.dispose()

        peerConnection?.close()
        peerConnection?.dispose()
        peerConnection = null

        wsManager.leaveRoom(currentRoomId)
        wsManager.disconnect()

        _isLocalVideoOn.value = true
        _isMicOn.value = false
        _isPeerConnected.value = false
        _localVideoTrack.value = null
        _remoteVideoTrack.value = null
    }

    // ─── Cleanup ──────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        localVideoSource?.dispose()
        localAudioSource?.dispose()
        peerConnectionFactory?.dispose()
        wsManager.disconnect()
        _localPreviewTrack.value?.dispose()  // ADD THIS
    }
}