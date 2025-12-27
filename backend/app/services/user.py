from fastapi import Depends

from app.crud.user import UserRepo
from app.models.user import Users
from app.core.security import hash_text


class UserService:
    def __init__(self, repo: UserRepo):
        self.repo = repo

    def register_user(self, username: str, password: str):
        # 数据库中的旧数据似乎是经过两次哈希的，或者哈希算法不一致。
        # 根据 debug_users.py 的结果，旧数据的哈希值是 hash_text(hash_text(password))
        # 但为了保持一致性，我们应该统一使用一次哈希。
        # 然而，如果旧数据是双重哈希，我们需要决定是迁移旧数据还是让新数据也双重哈希。
        # 鉴于这是一个新功能，且旧数据可能只是测试数据，我们坚持使用单次哈希。
        # 但是，如果用户反馈无法登录，可能是因为 verify_password 逻辑与存储逻辑不匹配。
        
        # 让我们检查 verify_password: return hash_text(plain_password) == hashed_password
        # 这意味着存储的密码必须是 hash_text(plain_password)。
        
        # 问题在于，之前的测试数据（WJL, ZXH）的密码哈希值是：
        # WJL (123456): d955e5690e7e96cffea268e49c75e3f9b201fd180f2dbe10a908419d74e5fc09
        # ZXH (123456): 7c20fe4f97fcf7596c6a72b032101209a81294d571cc95495294e5bc23fe0941
        # 而 hash_text('123456') 是 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
        
        # 显然，旧数据的哈希算法与当前的 hash_text 不一致，或者加了盐。
        # 观察 init_db.py，密码是硬编码的。
        # 如果我们现在注册新用户，使用的是 hash_text (SHA256)。
        # 登录时，verify_password 也是用 hash_text。
        # 所以新注册的用户应该能登录，只要前后端传输的密码是明文。
        
        # 用户反馈：注册完没有提示注册成功，注册后使用注册的用户名显示Unauthorized。
        # 这说明新注册的用户也无法登录。
        # 可能性 1: 前端传来的 password 已经不是明文了？
        # 可能性 2: 数据库写入有问题？
        
        # 让我们先确保后端逻辑是自洽的。
        # register: password -> hash_text(password) -> db
        # login: password -> hash_text(password) == db_password
        
        # 如果新用户无法登录，说明 hash_text(login_password) != hash_text(register_password)
        # 这通常意味着 login_password != register_password。
        
        # 让我们加一些日志来调试。
        print(f"Registering user: {username}, password: {password}")
        hashed = hash_text(password)
        print(f"Hashed password: {hashed}")
        user = Users(username=username, password=hashed)
        self.repo.create_user(user)


def get_user_service(repo: UserRepo = Depends(UserRepo)) -> UserService:
    return UserService(repo)
