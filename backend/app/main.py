from contextlib import asynccontextmanager
import logging
from fastapi import FastAPI, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from backend.app.config import PROJECT_NAME, VERSION, DESCRIPTION, CORS_ORIGINS
from backend.app.routes import health_router, predict_router
from backend.app.services.inference_service import get_inference_service

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("FreshIQ.API")


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application lifespan manager.
    Loads Phase 1A and Phase 1B ML models once at server startup.
    """
    logger.info("Initializing FreshIQ ML Simulation Engine...")
    service = get_inference_service()
    service.initialize()
    logger.info("FreshIQ ML models loaded successfully.")
    yield
    logger.info("FreshIQ API shutting down. Releasing resources...")


app = FastAPI(
    title=PROJECT_NAME,
    version=VERSION,
    description=DESCRIPTION,
    lifespan=lifespan
)

# CORS Middleware configuration for React/TypeScript client
app.add_middleware(
    CORSMiddleware,
    allow_origins=CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.error(f"Unhandled error processing {request.method} {request.url}: {str(exc)}", exc_info=True)
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={"detail": "Internal server error occurred while processing produce prediction."}
    )


# Register API routers with /api prefix
app.include_router(health_router, prefix="/api")
app.include_router(predict_router, prefix="/api")


@app.get("/", tags=["Root"])
async def root():
    return {
        "service": PROJECT_NAME,
        "version": VERSION,
        "status": "online",
        "docs_url": "/docs",
        "health_check": "/api/health",
        "predict_endpoint": "/api/predict"
    }
