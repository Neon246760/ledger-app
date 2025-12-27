from fastapi import APIRouter, HTTPException, Depends, Query
from datetime import datetime
from typing import Optional

from fastapi import APIRouter, HTTPException, Depends, Query
from datetime import datetime
from typing import Optional

from ..schemas.transaction import (
    TransactionCreate,
    TransactionUpdate,
    TransactionResponse,
    TransactionListResponse,
    TransactionSummaryResponse
)
from ..services.transaction import TransactionService, get_transaction_service
from ..core.security import get_current_user
from ..crud.user import UserRepo

router = APIRouter(prefix="/transactions", tags=["transactions"])


def get_user_id(current_username: str = Depends(get_current_user), repo: UserRepo = Depends(UserRepo)) -> int:
    """从token中获取用户名，然后获取用户ID"""
    user = repo.find_user_by_username(current_username)
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return user.id


@router.post("", response_model=TransactionResponse, status_code=201)
def create_transaction(
    transaction: TransactionCreate,
    user_id: int = Depends(get_user_id),
    service: TransactionService = Depends(get_transaction_service)
):
    """创建新的交易记录"""
    return service.create_transaction(
        user_id=user_id,
        type=transaction.type,
        amount=transaction.amount,
        category=transaction.category,
        description=transaction.description,
        image_path=transaction.image_path,
        date=transaction.date
    )


@router.get("", response_model=TransactionListResponse)
def get_transactions(
    skip: int = Query(0, ge=0, description="跳过的记录数"),
    limit: int = Query(100, ge=1, le=1000, description="返回的记录数"),
    type: Optional[str] = Query(None, description="交易类型: 'income' 或 'expense'"),
    category: Optional[str] = Query(None, description="分类筛选"),
    start_date: Optional[datetime] = Query(None, description="开始日期"),
    end_date: Optional[datetime] = Query(None, description="结束日期"),
    user_id: int = Depends(get_user_id),
    service: TransactionService = Depends(get_transaction_service)
):
    """获取交易记录列表"""
    transactions = service.get_transactions(
        user_id=user_id,
        skip=skip,
        limit=limit,
        type=type,
        category=category,
        start_date=start_date,
        end_date=end_date
    )
    total = service.repo.count_user_transactions(
        user_id=user_id,
        type=type,
        category=category,
        start_date=start_date,
        end_date=end_date
    )
    return TransactionListResponse(
        total=total,
        items=[TransactionResponse.model_validate(t) for t in transactions]
    )


@router.get("/{transaction_id}", response_model=TransactionResponse)
def get_transaction(
    transaction_id: int,
    user_id: int = Depends(get_user_id),
    service: TransactionService = Depends(get_transaction_service)
):
    """获取单个交易记录"""
    return service.get_transaction(transaction_id, user_id)


@router.put("/{transaction_id}", response_model=TransactionResponse)
def update_transaction(
    transaction_id: int,
    transaction_update: TransactionUpdate,
    user_id: int = Depends(get_user_id),
    service: TransactionService = Depends(get_transaction_service)
):
    """更新交易记录"""
    return service.update_transaction(
        transaction_id=transaction_id,
        user_id=user_id,
        type=transaction_update.type,
        amount=transaction_update.amount,
        category=transaction_update.category,
        description=transaction_update.description,
        date=transaction_update.date
    )


@router.delete("/{transaction_id}", status_code=204)
def delete_transaction(
    transaction_id: int,
    user_id: int = Depends(get_user_id),
    service: TransactionService = Depends(get_transaction_service)
):
    """删除交易记录"""
    service.delete_transaction(transaction_id, user_id)
    return None


@router.get("/summary/statistics", response_model=TransactionSummaryResponse)
def get_summary(
    start_date: Optional[datetime] = Query(None, description="开始日期"),
    end_date: Optional[datetime] = Query(None, description="结束日期"),
    user_id: int = Depends(get_user_id),
    service: TransactionService = Depends(get_transaction_service)
):
    """获取交易统计摘要（总收入、总支出、余额）"""
    summary = service.get_summary(user_id, start_date, end_date)
    return TransactionSummaryResponse(**summary)

