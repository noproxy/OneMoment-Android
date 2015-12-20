# Sync Process


## Preface

1. Video: data from server
2. Moment: data in local database. update moment means update the database. but may not download video file.


## Need

- download moment from server
- upload moment to server
- keep the new one if conflict
- check moment integrate


## Process

1. download user's videos list from server
2. sort videos list, older is first
3. map videos list to a hash map: ensure one video one day, remove the old video at same day
4. read user's moment list from database
5. compare moment to video map:

    for each moment, if in that day, there is
    1. no video:
        - upload this moment
    2. a video, then compare moment to video, if
        1. moment newer:
            - upload this moment
            - delete this video
            - *remove video from map*

        2. same:
            - *remove video from map*

        3. video newer:
            - use these video to update the moment information(but not download video)
            - *remove video from map*

6. if the left video map not empty, for each
    -   add a moment to moment list

7. for each in moment list, if moment's video file and thumbnails:
    1.  not exist, download video and create thumbnails
    2. exist, do nothing

8. sync ok.