from fastapi import APIRouter, UploadFile, File, HTTPException
import shutil
import os
import uuid
from datetime import datetime

router = APIRouter(prefix="/upload", tags=["upload"])

UPLOAD_DIR = "uploads"

# Ensure upload directory exists
if not os.path.exists(UPLOAD_DIR):
    os.makedirs(UPLOAD_DIR)

@router.post("/image", response_model=dict)
async def upload_image(file: UploadFile = File(...)):
    """
    上传图片文件
    返回: {"url": "/uploads/filename.ext"}
    """
    # Validate file type
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")
    
    # Generate unique filename
    file_ext = os.path.splitext(file.filename)[1]
    if not file_ext:
        # Default extension if missing
        if file.content_type == "image/jpeg":
            file_ext = ".jpg"
        elif file.content_type == "image/png":
            file_ext = ".png"
        else:
            file_ext = ".jpg"
            
    filename = f"{datetime.now().strftime('%Y%m%d')}_{uuid.uuid4().hex}{file_ext}"
    file_path = os.path.join(UPLOAD_DIR, filename)
    
    try:
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Could not save file: {str(e)}")
        
    # Return the relative URL path
    # Note: The frontend should prepend the base URL
    return {"url": f"/uploads/{filename}"}
