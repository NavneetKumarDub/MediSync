import { Router } from 'express';
import { getRoomMetadata ,getOrCreateChatRoom , getInbox,getRoomMessages} from '../controllers/chat.controller';
import { authMiddleware } from '../middlewares/auth.middleware'; 

const router = Router();

router.get('/:roomId/metadata', authMiddleware, getRoomMetadata);


router.use(authMiddleware);

router.get('/:roomId/metadata', getRoomMetadata);
router.post('/room', getOrCreateChatRoom);
router.get('/inbox', getInbox);
router.get('/room/:roomId/messages', getRoomMessages);

export default router;