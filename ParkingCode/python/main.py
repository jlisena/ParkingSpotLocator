import yaml
import numpy as np
import cv2
from coordinates_generator import CoordinatesGenerator
import pyrebase

fbConfig = {
    "apiKey": "AIzaSyD8BhJzceCv22jJtHyl2fk5eeNJWwaJVu0",
    "authDomain": "decent-trail-243702.firebaseapp.com",
    "databaseURL": "https://decent-trail-243702.firebaseio.com",
    "projectId": "decent-trail-243702",
    "storageBucket": "decent-trail-243702.appspot.com",
    "messagingSenderId": "37676767767",
    "appId": "1:37676767767:web:7e7a276cab19843d"
}

# initialize database with configuration information
firebase = pyrebase.initialize_app(fbConfig)
populateDB = firebase.database()

config = {#'video_file_in': r"../datasets/input2.mp4",
    'video_file_in': r"../datasets/goodvid.mp4",
    'video_file_out': r"../datasets/output.mp4",
    #'draw_still_image': r"../datasets/parkinglot_1_snapshot.png",
    'draw_still_image': r"../datasets/goodimg.png",
    'custom_yaml_file': r"../datasets/parking.yml",
    'yaml_file': r"../datasets/parking2.yml",
    'video_save': False,
    'text_overlay': True,
    'parking_overlay': True,
    'parking_id_overlay': True,
    'coordinate_generation': False,
    'parking_detection': True,
    'draw_red_line_overlay': True,
    'park_sec_to_wait': 2.7,
    'write_to_db_wait': 100,
    'video_speed_waitKey': 67, #14 good
    'start_frame': 0}

# Set capture device or file
videoCapture = cv2.VideoCapture(config['video_file_in'])

video_info = {'fps':    videoCapture.get(cv2.CAP_PROP_FPS),
              'width':  int(videoCapture.get(cv2.CAP_PROP_FRAME_WIDTH)),
              'height': int(videoCapture.get(cv2.CAP_PROP_FRAME_HEIGHT)),
              'fourcc': videoCapture.get(cv2.CAP_PROP_FOURCC),
              'num_of_frames': int(videoCapture.get(cv2.CAP_PROP_FRAME_COUNT))}

# allows for coordinate generation
if config['coordinate_generation']:
    # write YAML data
    with open(config['custom_yaml_file'], "w+") as points:
        generator = CoordinatesGenerator(config['draw_still_image'], points, (255, 0, 0))
        generator.generate()
    # Read YAML data (parking space polygons)
    with open(config['custom_yaml_file'], 'r') as stream:
        parking_data = yaml.load(stream, Loader=yaml.FullLoader)
        parking_bounding_rects = []
else:
    # Read YAML data (parking space polygons)
    with open(config['yaml_file'], 'r') as stream:
        parking_data = yaml.load(stream, Loader=yaml.FullLoader)
        parking_bounding_rects = []

for park in parking_data:
    points = np.array(park['points'])
    rect = cv2.boundingRect(points)
    parking_bounding_rects.append(rect)

# define encoder if video saving is enabled
if config['video_save']:
    # define codec for mp4 files (H264 or DIVX or mp4v)
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    # create and object
    out = cv2.VideoWriter(config['video_file_out'], fourcc, video_info['fps'], (video_info['width'], video_info['height']))

# initialize video data
parking_status = [False]*len(parking_data)
parking_buffer = [None]*len(parking_data)
videoCapture.set(cv2.CAP_PROP_POS_FRAMES, config['start_frame'])

# counter used to write to db every x(write_to_db_wait) frames
write_to_db_counter = 0
while videoCapture.isOpened():
    # initialize data
    spots = 0
    occupied = 0

    # Read frame-by-frame and get current position of the video file in seconds
    video_cur_pos = videoCapture.get(cv2.CAP_PROP_POS_MSEC) / 1000.0
    # Index of the frame to be decoded/captured next
    video_cur_frame = videoCapture.get(cv2.CAP_PROP_POS_FRAMES)
    ret, frame = videoCapture.read()
    if ret is False:
        break

    frame_blur = cv2.GaussianBlur(frame.copy(), (5, 5), 3)
    frame_gray = cv2.cvtColor(frame_blur, cv2.COLOR_BGR2GRAY)
    frame_out = frame.copy()

    if config['parking_detection']:
        for ind, park in enumerate(parking_data):
            points = np.array(park['points'])
            rect = parking_bounding_rects[ind]
            roi_gray = frame_gray[rect[1]:(rect[1]+rect[3]), rect[0]:(rect[0]+rect[2])]
            # Gray scale values inside the region of interest
            status = np.std(roi_gray) < 22 and np.mean(roi_gray) > 53
            # If detected a change in parking status, save the current time
            if status is not parking_status[ind] and parking_buffer[ind] is None:
                parking_buffer[ind] = video_cur_pos
            # If status is still different than the one saved and counter is open
            elif status is not parking_status[ind] and parking_buffer[ind]is not None:
                if video_cur_pos - parking_buffer[ind] > config['park_sec_to_wait']:
                    parking_status[ind] = status
                    parking_buffer[ind] = None
            # If status is still same and counter is open
            elif status == parking_status[ind] and parking_buffer[ind]is not None:
                parking_buffer[ind] = None
            # print(parking_status)

    if config['parking_overlay']:
        for ind, park in enumerate(parking_data):
            points = np.array(park['points'])
            if parking_status[ind]:
                color = (0, 255, 0)
                spots = spots+1
                cv2.drawContours(frame_out, [points], contourIdx=-1, color=color, thickness=2, lineType=cv2.LINE_8)
            else:
                occupied = occupied+1
                if config['draw_red_line_overlay']:
                    color = (0, 0, 255)  # fix this sometime
                    cv2.drawContours(frame_out, [points], contourIdx=-1, color=color, thickness=2, lineType=cv2.LINE_8)

            moments = cv2.moments(points)
            centroid = (int(moments['m10']/moments['m00'])-3, int(moments['m01']/moments['m00'])+3)
            cv2.putText(frame_out, str(park['id']), (centroid[0]+1, centroid[1]+1), cv2.FONT_HERSHEY_SIMPLEX, 0.4,
                        (255, 255, 255), 1, cv2.LINE_AA)
            cv2.putText(frame_out, str(park['id']), (centroid[0]-1, centroid[1]-1), cv2.FONT_HERSHEY_SIMPLEX, 0.4,
                        (255, 255, 255), 1, cv2.LINE_AA)
            cv2.putText(frame_out, str(park['id']), (centroid[0]+1, centroid[1]-1), cv2.FONT_HERSHEY_SIMPLEX, 0.4,
                        (255, 255, 255), 1, cv2.LINE_AA)
            cv2.putText(frame_out, str(park['id']), (centroid[0]-1, centroid[1]+1), cv2.FONT_HERSHEY_SIMPLEX, 0.4,
                        (255, 255, 255), 1, cv2.LINE_AA)
            cv2.putText(frame_out, str(park['id']), centroid, cv2.FONT_HERSHEY_SIMPLEX, 0.4, (0, 0, 0), 1, cv2.LINE_AA)

    # print parking lot 1 information to database
    write_to_db_counter = write_to_db_counter + 1
    # print write_to_db_counter
    if write_to_db_counter % config['write_to_db_wait'] == 0:
        populateDB.child().update({"parkingLot1": spots})

    # Draw Overlay
    if config['text_overlay']:
        cv2.rectangle(frame_out, (1, 5), (180, 0), (255, 255, 255), 90) #this is the write rectangle
        str_on_frame = "Video Frames: %d/%d" % (video_cur_frame, video_info['num_of_frames'])
        cv2.putText(frame_out, str_on_frame, (5, 15), cv2.FONT_HERSHEY_COMPLEX_SMALL, 0.7, (255, 128, 0), 1, cv2.LINE_AA)
        str_on_frame = "Spots: %d Occupied: %d" % (spots, occupied)
        cv2.putText(frame_out, str_on_frame, (5, 40), cv2.FONT_HERSHEY_COMPLEX_SMALL, 0.7, (255, 128, 0), 1, cv2.LINE_AA)

    # Video display
    k = cv2.waitKey(config['video_speed_waitKey'])  # 67ms delay = (1000ms/15fps)
    if k == ord('q'):
        break

    # video control to screenshot one frame
    elif k == ord('c'):
        cv2.imwrite('frame%d.jpg' % video_cur_frame, frame_out)

    # video control to fast forward 50 frames
    elif k == ord('d'):
        videoCapture.set(cv2.CAP_PROP_POS_FRAMES, video_cur_frame+100)

    # video control to rewind 50 frames
    elif k == ord('a'):
        videoCapture.set(cv2.CAP_PROP_POS_FRAMES, video_cur_frame-100)

    # video control to pause at current frame
    elif k == ord('p'):
        while True:
            key2 = cv2.waitKey(1) or 0xff
            cv2.imshow('Lot 47', frame_out)

            if key2 == ord('p'):
                break

    # start video feed
    cv2.imshow('Lot 47', frame_out)

    # save video if enabled
    if config['video_save']:
        # takes video every 3 video frames
        if video_cur_frame % 3 == 0:
            out.write(frame_out)

if config['video_save']:
    out.release()

videoCapture.release()
cv2.destroyAllWindows()
