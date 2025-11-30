from fastapi import APIRouter, HTTPException, Depends
from fastapi.security import OAuth2PasswordRequestForm

from schemas.user import RegisterRequest
from services.user import UserService, get_user_service
from core.security import get_current_user, verify_password, create_access_token

router = APIRouter()


@router.post("/register")
def register_user(payload: RegisterRequest, service: UserService = Depends(get_user_service)):
    if payload.password != payload.repeat_password:
        raise HTTPException(status_code=400, detail="Passwords do not match")
    service.register_user(payload.username, payload.password)
    return {"detail": "User registered successfully"}


@router.post("/login")
def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends(), service: UserService = Depends(get_user_service)):
    user = service.repo.find_user_by_username(form_data.username)
    if user is None or not verify_password(form_data.password, user.password):
        raise HTTPException(status_code=401, detail="Incorrect username or password")
    access_token = create_access_token(data={"sub": user.username})
    return {"access_token": access_token, "token_type": "bearer"}
