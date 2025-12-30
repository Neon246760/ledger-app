from fastapi import APIRouter, HTTPException, Depends
from fastapi.security import OAuth2PasswordRequestForm

from app.schemas.user import RegisterRequest, ChangePasswordRequest, DeleteAccountRequest
from app.services.user import UserService, get_user_service
from app.core.security import get_current_user, verify_password, create_access_token, hash_text

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
    print(f"Login attempt for: {form_data.username}, password: {form_data.password}")
    if user:
        print(f"User found. Stored hash: {user.password}")
        print(f"Computed hash: {hash_text(form_data.password)}")
        print(f"Match: {verify_password(form_data.password, user.password)}")
    
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
