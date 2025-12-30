from fastapi import Depends

from app.crud.user import UserRepo
from app.models.user import Users
from app.core.security import hash_text
from app.crud.transaction import TransactionRepo
from app.crud.ledger import LedgerRepo
import os


class UserService:
    def __init__(self, repo: UserRepo):
        self.repo = repo

    def register_user(self, username: str, password: str):
        
        print(f"Registering user: {username}, password: {password}")
        hashed = hash_text(password)
        print(f"Hashed password: {hashed}")
        user = Users(username=username, password=hashed)
        self.repo.create_user(user)

    def change_password(self, username: str, password: str, repeat_password: str):
        if password != repeat_password:
            from fastapi import HTTPException
            raise HTTPException(status_code=400, detail="Passwords do not match")
        user = self.repo.find_user_by_username(username)
        if user is None:
            from fastapi import HTTPException
            raise HTTPException(status_code=404, detail="User not found")
        hashed = hash_text(password)
        self.repo.update_password(user, hashed)
        return {"detail": "Password updated successfully"}

    def delete_account(self, username: str, password: str, avatar_url: str | None = None):
        from fastapi import HTTPException
        from app.core.security import verify_password
        user = self.repo.find_user_by_username(username)
        if user is None:
            raise HTTPException(status_code=404, detail="User not found")
        if not verify_password(password, user.password):
            raise HTTPException(status_code=401, detail="Incorrect password")
        # Delete related resources
        tx_repo = TransactionRepo(self.repo.session)
        ledger_repo = LedgerRepo(self.repo.session)
        tx_repo.delete_all_for_user(user_id=user.id)
        ledger_repo.delete_all_for_user(user_id=user.id)
        # Delete avatar file if provided
        if avatar_url and avatar_url.startswith("/uploads/"):
            filename = os.path.basename(avatar_url)
            file_path = os.path.join("uploads", filename)
            try:
                if os.path.exists(file_path):
                    os.remove(file_path)
            except Exception:
                pass
        # Delete user
        self.repo.delete_user(user)
        return {"detail": "Account deleted successfully"}

def get_user_service(repo: UserRepo = Depends(UserRepo)) -> UserService:
    return UserService(repo)
