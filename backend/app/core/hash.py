import hashlib


def hash_text(text: str) -> str:
    return hashlib.sha256(text.encode()).hexdigest()


if __name__ == "__main__":
    print(hash_text("1120233061"))
    print(hash_text("1120233278"))
