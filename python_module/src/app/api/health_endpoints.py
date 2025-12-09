from datetime import datetime

from fastapi import APIRouter

from .schemas import HealthResponse


router = APIRouter(prefix="/health", tags=["health"])


@router.get("", response_model=HealthResponse)
def health_check() -> HealthResponse:
    from datetime import timezone
    return HealthResponse(status="OK", timestamp=datetime.now(timezone.utc))

