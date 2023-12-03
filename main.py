from fastapi import FastAPI, File, UploadFile
from fastapi.responses import StreamingResponse
import numpy as np
from skimage.segmentation import slic
from skimage import io
from fastapi import FastAPI, File, UploadFile, Form
from skimage.segmentation import find_boundaries
from skimage.util import img_as_float
import shutil
from shapely.geometry import MultiPoint
import json

class ImageProcessor:
    def __init__(self, file_path: str):
        self.file_path = file_path

    
    def process_image(self, file: UploadFile, n_segments: str, opacity: int):
        with open(self.file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
        print(file.filename)

        n_segments = [int(e) for e in n_segments[1:-1].split(',')]

        print(n_segments)
        print(opacity)

        image = io.imread(self.file_path)
        image = img_as_float(image)

        if image.shape[2] == 4:
            image = image[:, :, :3]

        return self.polygons_info(image, n_segments, opacity)


    def polygons_info(self, image, n_segments, opacity):
        for i in range(len(n_segments)):
            segments = slic(image, n_segments=n_segments[i], compactness=15, sigma=1)
            for segment_label in np.unique(segments):
                segment_pixels_color = image[segments == segment_label]
                average_color = np.mean(segment_pixels_color, axis=0)

                boundary = find_boundaries(segments == segment_label)
                boundary_pixels = np.where(boundary)

                edge_pixels = list(zip(boundary_pixels[1], boundary_pixels[0]))
                multi_point = MultiPoint(edge_pixels)
                convex_hull_polygon = multi_point.convex_hull
                polygon_points = list(convex_hull_polygon.exterior.coords)

                color = [int(average_color[0] * 255), int(average_color[1] * 255), int(average_color[2] * 255), opacity]

                yield json.dumps({"polygon_points": polygon_points, "color": color}) + '\n'


imageProcessor = ImageProcessor('image.png')

app = FastAPI()

@app.post("/process_image/")
async def process_image_endpoint(file: UploadFile = File(...), n_segments: str = Form(...), opacity: int = Form(...)):
    return StreamingResponse(imageProcessor.process_image(file, n_segments, opacity), media_type="application/x-ndjson")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
