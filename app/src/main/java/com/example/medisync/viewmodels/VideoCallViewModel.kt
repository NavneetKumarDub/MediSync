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

import org.webrtc.*
import com.example.medisync.networks.VideoSignalEvent
import com.example.medisync.networks.VideoWebSocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoCallViewModel(application: Application) : AndroidViewModel(application) {

    // ─── UI State Flows ───────────────────────────────────────────────────────
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
    // FIXED: Single shared EGL context for ALL renderers and capture pipeline.
    // Using two separate EglBase contexts means the GPU texture handles are
    // invisible to each other — which caused the blank local PiP preview.
    val eglBaseContext: EglBase.Context by lazy { EglBase.create().eglBaseContext }

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
    private var isNegotiating = false

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

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
                Log.d("VideoVM", "ICE connection state: $newState")
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {}
            override fun onSignalingChange(newState: PeerConnection.SignalingState) {}
            override fun onDataChannel(dataChannel: DataChannel) {}
            override fun onRenegotiationNeeded() {
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

        localAudioTrack?.let { peerConnection?.addTrack(it) }
        _localVideoTrack.value?.let { peerConnection?.addTrack(it) }
        val currentTrack = _localVideoTrack.value
        _localVideoTrack.value = null
        _localVideoTrack.value = currentTrack
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
                        isNegotiating = false

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

    // ─── Local Video Setup ────────────────────────────────────────────────────
    private fun initializeWebRTC(context: Context) {
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val options = PeerConnectionFactory.Options()
        // FIXED: Both encoder and decoder now use the same single eglBaseContext
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

        // FIXED: SurfaceTextureHelper uses the same shared eglBaseContext
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
        videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource!!.capturerObserver)
        videoCapturer?.startCapture(1280, 720, 30)

        // FIXED: Single video track — no separate "preview track" needed.
        // The same track can have multiple sinks added to it (local renderer + peer connection).
        val videoTrack = factory.createVideoTrack("local_video_track", localVideoSource)
        _localVideoTrack.value = videoTrack

        Log.d("VideoVM", "Local video track created: $videoTrack")
    }

    private fun createCameraCapturer(context: Context): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) return enumerator.createCapturer(deviceName, null)
        }
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName)) return enumerator.createCapturer(deviceName, null)
        }
        return null
    }

    // ─── User Actions ─────────────────────────────────────────────────────────
    fun toggleMic() {
        _isMicOn.value = !_isMicOn.value
        localAudioTrack?.setEnabled(_isMicOn.value)
    }

    fun toggleVideo() {
        _isLocalVideoOn.value = !_isLocalVideoOn.value
        _localVideoTrack.value?.setEnabled(_isLocalVideoOn.value)
    }

    fun flipCamera() {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {}
            override fun onCameraSwitchError(errorDescription: String) {}
        })
    }
    fun reattachLocalSink() {
        val currentTrack = _localVideoTrack.value
        _localVideoTrack.value = null
        _localVideoTrack.value = currentTrack
    }

    // ─── End Call ─────────────────────────────────────────────────────────────
    fun endCall() {
        viewModelScope.launch(Dispatchers.IO) {  // ← off the main thread
            try { videoCapturer?.stopCapture() } catch (e: Exception) { e.printStackTrace() }

            // Disable tracks before closing — stops frame flow cleanly
            _localVideoTrack.value?.setEnabled(false)

            peerConnection?.close()
            // Do NOT call dispose() here — let onCleared() handle full teardown
            // after the ViewModel lifecycle ends naturally

            wsManager.leaveRoom(currentRoomId)
            wsManager.disconnect()

            withContext(Dispatchers.Main) {
                _isPeerConnected.value = false
                _remoteVideoTrack.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            try { videoCapturer?.stopCapture() } catch (e: Exception) { }
            videoCapturer?.dispose()
            videoCapturer = null

            localVideoSource?.dispose()
            localAudioSource?.dispose()

            peerConnection?.dispose()
            peerConnection = null

            peerConnectionFactory?.dispose()
            peerConnectionFactory = null

            wsManager.disconnect()
            _localVideoTrack.value?.dispose()
        }
    }
}