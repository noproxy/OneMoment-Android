package co.yishun.onemoment.app.api.modelv4;

/**
 * Created by Jinge on 2016/1/25.
 */
public class TodayWorld {
    /*
    "ranking": 1453709845,
"create_time": 1453709845,
"thumbnail": "http://privateworldvideo.qiniu.yishun.co/vframe-videoworld-world2-55d0559d7d40b567a7328bc4-1453713184.png?imageMogr2/blur/5x5&e=1453734099&token=1-ZM6fcKIPEBVshewXDTEbs8nC7-4UkhLUnj3eGW:Ee_Tv-sWEuhHPetQcI4jgbn2FJ4=",
"_id": "56a5da15005653dcf0a2f20e",
"available": true,
"name": "2016-01-25",
"videos_num": 7
     */
    public String _id;
    public int ranking;
    public int createTime;
    public String thumbnail;
    public boolean available;
    public String name;
    public int videoNum;
}
