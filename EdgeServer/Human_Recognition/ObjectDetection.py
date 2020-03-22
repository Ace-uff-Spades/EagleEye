from imageai.Detection import ObjectDetection

from PIL import Image
from PIL.ExifTags import TAGS, GPSTAGS

import os

#GPS getting GPS coodinates from image
def get_exif(filename):
    exif = Image.open(filename)._getexif()

    if exif is not None:
        for key, value in exif.items():
            name = TAGS.get(key, key)
            exif[name] = exif.pop(key)

        if 'GPSInfo' in exif:
            for key in exif['GPSInfo'].keys():
                name = GPSTAGS.get(key,key)
                exif['GPSInfo'][name] = exif['GPSInfo'].pop(key)
    else:
    	return "no GPS data"
    return exif

#convert to human-readable GPS coordinates
def get_coordinates(info):
    for key in ['Latitude', 'Longitude']:
        if 'GPS'+key in info and 'GPS'+key+'Ref' in info:
            e = info['GPS'+key]
            ref = info['GPS'+key+'Ref']
            info[key] = ( str(e[0][0]/e[0][1]) + '°' +
                          str(e[1][0]/e[1][1]) + '′' +
                          str(e[2][0]/e[2][1]) + '″ ' +
                          ref )
    if 'Latitude' in info and 'Longitude' in info:
        return [info['Latitude'], info['Longitude']]

#convert to decimal GPS coordinates
def get_decimal_coordinates(info):
    for key in ['Latitude', 'Longitude']:
        if 'GPS'+key in info and 'GPS'+key+'Ref' in info:
            e = info['GPS'+key]
            ref = info['GPS'+key+'Ref']
            info[key] = ( e[0][0]/e[0][1] +
                          e[1][0]/e[1][1] / 60 +
                          e[2][0]/e[2][1] / 3600
                        ) * (-1 if ref in ['S','W'] else 1)

    if 'Latitude' in info and 'Longitude' in info:
        return [info['Latitude'], info['Longitude']]

model_path = os.getcwd()
input_path = os.path.join(model_path, "input")
output_path = os.path.join(model_path, "output")

detector = ObjectDetection()
detector.setModelTypeAsRetinaNet()
detector.setModelPath( os.path.join(model_path , "resnet50_coco_best_v2.0.1.h5"))
detector.loadModel()
custom = detector.CustomObjects(person=True)

for filename in os.listdir(input_path):
	if filename.endswith(".jpg"):
		detections = detector.detectCustomObjectsFromImage(custom_objects=custom, input_image=os.path.join(input_path , filename), output_image_path=os.path.join(output_path , "new" + filename), minimum_percentage_probability=30)
		print(filename)
		for eachObject in detections:
			print(eachObject["name"] , " : " , eachObject["percentage_probability"])
			if get_exif(os.path.join(input_path , filename)) != "no GPS data":
				print(get_decimal_coordinates(get_exif(os.path.join(input_path , filename))['GPSInfo']))
		continue
	else:
		continue
