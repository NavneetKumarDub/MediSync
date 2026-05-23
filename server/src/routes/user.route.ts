import { Router } from "express";
import { registerUser ,saveFcmToken} from "../controllers/user.controller";
import { authMiddleware } from "../middlewares/auth.middleware";


const router = Router();

router.post("/register",registerUser);
router.post("/fcm-token", authMiddleware, saveFcmToken);


export default router;