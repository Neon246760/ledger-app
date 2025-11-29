from fastapi import APIRouter, HTTPException, Depends

from schemas.user import RegisterRequest
from services.user import UserService, get_user_service

router = APIRouter()


@router.post("/register")
def register_user(payload: RegisterRequest, service: UserService = Depends(get_user_service)):
    if payload.password != payload.repeat_password:
        raise HTTPException(status_code=400, detail="Passwords do not match")
    service.register_user(payload.username, payload.password)
    return {"detail": "User registered successfully"}
