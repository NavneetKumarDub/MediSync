export interface VideoSignal {
  
  type: 'join-room' | 'leave-room' | 'offer' | 'answer' | 'ice-candidate' | 'user-joined' | 'room-joined' | 'user-left' | 'renegotiate';
  roomId: number;

  id?: string;          
  targetId?: string;      
  
  sdp?: {
      type: 'offer' | 'answer' | 'pranswer' | 'rollback';
      sdp: string;
  }; 
  candidate?: {
      candidate: string;
      sdpMLineIndex?: number | null;
      sdpMid?: string | null;
  };
}