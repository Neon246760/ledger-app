from sqlmodel import select

from app.models.profile import UserProfile
from app.core.db import SessionDep


class ProfileRepo:
    def __init__(self, session: SessionDep):
        self.session = session

    def get_by_user_id(self, user_id: int) -> UserProfile | None:
        return self.session.exec(select(UserProfile).where(UserProfile.user_id == user_id)).first()

    def upsert_avatar(self, user_id: int, avatar_url: str | None) -> UserProfile:
        profile = self.get_by_user_id(user_id)
        if profile is None:
            profile = UserProfile(user_id=user_id, avatar_url=avatar_url)
            self.session.add(profile)
            self.session.commit()
            self.session.refresh(profile)
            return profile
        profile.avatar_url = avatar_url
        self.session.add(profile)
        self.session.commit()
        self.session.refresh(profile)
        return profile
