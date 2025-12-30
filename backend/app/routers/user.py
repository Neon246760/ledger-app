from fastapi import APIRouter, HTTPException, Depends
from fastapi.security import OAuth2PasswordRequestForm

from app.schemas.user import RegisterRequest, ChangePasswordRequest, DeleteAccountRequest, UserUpdate, UserResponse
from app.services.user import UserService, get_user_service
from app.core.security import get_current_user, verify_password, create_access_token
from app.models.user import Users

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


@router.post("/user/password")
def update_password(
    payload: ChangePasswordRequest,
    current_username: str = Depends(get_current_user),
    service: UserService = Depends(get_user_service)
):
    return service.change_password(current_username, payload.password, payload.repeat_password)


@router.post("/user/delete")
def delete_account(
    payload: DeleteAccountRequest,
    current_username: str = Depends(get_current_user),
    service: UserService = Depends(get_user_service)
):
    return service.delete_account(current_username, payload.password, payload.avatar_url)


@router.get("/me", response_model=UserResponse)
def read_users_me(
    current_username: str = Depends(get_current_user),
    service: UserService = Depends(get_user_service)
):
    user = service.repo.find_user_by_username(current_username)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.put("/me", response_model=UserResponse)
def update_user_me(
    user_update: UserUpdate,
    current_username: str = Depends(get_current_user),
    service: UserService = Depends(get_user_service)
):
    if user_update.avatar_path is not None:
        updated_user = service.update_user_avatar(current_username, user_update.avatar_path)
        return updated_user
    
    user = service.repo.find_user_by_username(current_username)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user
