from fastapi import FastAPI, Request

from app.api.analysis_endpoints import router as analysis_router
from app.api.prediction_endpoints import router as prediction_router
from app.api.reporting_endpoints import router as reporting_router
from app.api.health_endpoints import router as health_router
from app.api.data_endpoints import router as data_router


app = FastAPI(title="SZEBI Analysis & Prediction API")


# Simple middleware to log incoming requests and responses for debugging
@app.middleware("http")
async def log_requests(request: Request, call_next):
    try:
        body_bytes = await request.body()
        body_text = body_bytes.decode("utf-8", errors="ignore")
    except Exception:
        body_text = "<could not read body>"
    print(f"[FASTAPI LOG] Incoming: {request.method} {request.url.path} Headers={dict(request.headers)} Body={body_text}")
    response = await call_next(request)
    print(f"[FASTAPI LOG] Response status: {response.status_code} for {request.method} {request.url.path}")
    return response


app.include_router(analysis_router)
app.include_router(prediction_router)
app.include_router(reporting_router)
app.include_router(health_router)
app.include_router(data_router)


# Umożliwia uruchomienie: python -m app.main (z katalogu python_module/src)
if __name__ == "__main__":  # pragma: no cover
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=False)
