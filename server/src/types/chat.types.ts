export type IncomingMessage =
    | { type: 'chat:join';    data: { roomId: number } }
    | { type: 'chat:message'; data: { roomId: number; text: string; localId: string } } // Added localId
    | { type: 'chat:read';    data: { roomId: number; messageId: number } }
    | { type: 'chat:leave';   data: { roomId: number } }

export type OutgoingMessage =
    | { type: 'chat:joined';  data: { roomId: number } }
    | { type: 'chat:history'; data: { roomId: number; messages: any[] } } // Added history
    | { type: 'chat:message'; data: { 
        roomId: number; 
        messageId: number; 
        senderId: number; 
        text: string; 
        sentAt: string; 
        localId?: string // Added localId for confirmation
      } }
    | { type: 'chat:read';    data: { messageId: number } }
    | { type: 'error';        data: { message: string } }


export interface ConnectedClient {
    userId: number
    roomId: number | null
}