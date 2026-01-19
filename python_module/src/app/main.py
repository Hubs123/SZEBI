from fastapi import FastAPI

from app.api.analysis_endpoints import router as analysis_router
from app.api.prediction_endpoints import router as prediction_router
from app.api.reporting_endpoints import router as reporting_router
from app.api.health_endpoints import router as health_router
from app.api.data_endpoints import router as data_router


app = FastAPI(title="SZEBI Analysis & Prediction API")

app.include_router(analysis_router)
app.include_router(prediction_router)
app.include_router(reporting_router)
app.include_router(health_router)
app.include_router(data_router)


# Umożliwia uruchomienie: python -m app.main (z katalogu python_module/src)
if __name__ == "__main__":  # pragma: no cover
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=False)

