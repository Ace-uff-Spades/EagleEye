from imageai.Detection import ObjectDetection

from PIL import Image
from PIL.ExifTags import TAGS, GPSTAGS

import os
import json

#model initialization
model_path = os.getcwd()
input_path = os.path.join(model_path, "input")
output_path = os.path.join(model_path, "output")

detector = ObjectDetection()
detector.setModelTypeAsRetinaNet()
detector.setModelPath( os.path.join(model_path , "resnet50_coco_best_v2.0.1.h5"))
detector.loadModel()
custom = detector.CustomObjects(person=True)

#output initialization
i=-1
output = set()
pixel_coordinates = {}

#Iterate through the files in input and search for humans
for filename in os.listdir(input_path):
    if filename.endswith(".jpg"):
        detections = detector.detectCustomObjectsFromImage(custom_objects=custom, input_image=os.path.join(input_path , filename), output_image_path=os.path.join(output_path , "new" + filename), minimum_percentage_probability=50)
        i=i+1
        #if a human is detected, determine the pixel coordinate of their location and output true
        for eachObject in detections:
            x1, y1, x2, y2 = eachObject["box_points"]
            midpoint = "("+str((x1+x2)*.5) + ", " + str(y2)+")"
            #print(eachObject["name"] , " : " , eachObject["percentage_probability"],  " : ", midpoint)
            #if get_geotagging(get_exif(os.path.join(input_path, filename))) != "no GPS data":
                #print(get_coordinates(get_geotagging(get_exif(os.path.join(input_path, filename)))))
            output.add(i)
            pixel_coordinates.setdefault(i, [])
            pixel_coordinates[i].append(midpoint)
        continue
        continue
    else:
        continue

print(output, file=open("output/output.txt","w"))
#print(pixel_coordinates, file=open("output/pixel_coordinates.txt", "w"))
#save booleans and pixel coordinates to json files
#with open('output/output.json', 'w') as fp:
#   json.dump(output, fp)
with open('output/pixel_coordinates.json', 'w') as fp:
    json.dump(pixel_coordinates, fp)




