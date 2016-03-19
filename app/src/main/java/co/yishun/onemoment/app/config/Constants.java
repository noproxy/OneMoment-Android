package co.yishun.onemoment.app.config;

/**
 * Constants configures. <p> Created by Carlos on 2015/8/5.
 */
public class Constants {
    public static final boolean LOG_ENABLE = false;
    public static final boolean SANDBOX = false;
    public static final String API_KEY = "";
    public static final String API_V3_URL;
    public static final String API_V4_URL;
    public static final String AES_KEY = "QJBBNfrwp2oN4ZBwT9qZ4MGObN8y56bEydJj48L8xVs=";
    public static final String MIME_TYPE = "video/mp4";
    public static final String VIDEO_FILE_SUFFIX = ".mp4";
    public static final String THUMB_FILE_SUFFIX = ".png";
    public static final String URL_HYPHEN = "-";
    public static final String LONG_VIDEO_PREFIX = "long-";
    public static final String PROFILE_PREFIX = "avatar-";
    public static final String PROFILE_SUFFIX = ".png";
    public static final String WORLD_VIDEO_PREFIX = "videoworld-world2-";
    public static final String EXPORT_VIDEO_PREFIX = "yishunExport-";
    public static final String HYBRD_UNZIP_DIR = "hybrd";
    public static final String FILE_URL_PREFIX = "file://";
    public static final String APP_URL_PREFIX = "ysjs://";
    public static final String IDENTITY_DIR = "identity";
    public static final String IDENTITY_INFO_FILE_NAME = "info";
    public static final String TIME_FORMAT = "yyyyMMdd";
    public static final String TIEM_FORMAT_EXPORT = "yyyyMMdd_HHmmss";
    public static final String VIDEO_THUMB_STORE_DIR = "thumbs";
    public static final String HYBRID_ZIP_DOWNLOAD_URL;
    public static final int VIDEO_HEIGHT = 480;
    public static final int VIDEO_WIDTH = 480;
    public static final int VIDEO_FPS = 30;
    public static final int CODE_SUCCESS = 1;
    public static final int CODE_Fail = 0;
    public static final int CODE_PARAMETER_ABSENT = -1;
    public static final int CODE_PARAMETER_INVALID = -2;
    public static final int CODE_SERVER_ERROR = -3;
    public static final int CODE_TOO_FEQUENT = -5;
    public static final int INT_EXIT_DELAY_MILLIS = 500;
    public static final String QQ_APP_ID = "1104574591";
    public static final String WE_CHAT_APP_ID = "wx669ce174488102f4";
    public static final String WEIBO_APP_ID = "4070980764";
    public static final String MARKER_HEADER = "Om-Android-Market";

    static {
        //noinspection ConstantConditions
        API_V3_URL = SANDBOX ? "http://sandbox.api.yishun.co:53470/v3" : "http://api.yishun.co/v3";
        //noinspection ConstantConditions
        API_V4_URL = SANDBOX ? "http://sandbox.api.yishun.co:53470/v4" : "https://api.yishun" +
                ".co:53471/v4";
        //noinspection ConstantConditions
        HYBRID_ZIP_DOWNLOAD_URL = SANDBOX ? "http://sandbox.api.yishun.co:53470/hybrdstatic/zip/default.zip" : "https://api.yishun.co:53471/hybrdstatic/zip/default.zip";
    }

    public static class ErrorStr {
        public static final String SUCCESS = "Ok";
    }

    public static class ErrorCode {
        public static final int INPUT_MISSING = -1;
        public static final int INPUT_ERROR = -2;
        public static final int SERVER_ERROR = -3;
        public static final int API_REQUEST_TOO_FEQUENT = -5;
        public static final int API_KEY_IS_MISSING = -6;
        public static final int API_KEY_ERROR = -7;
        public static final int NOT_AUTH_REQUEST = -8;
        public static final int CANNOT_WRITE_DATABASE = -9;
        public static final int PHONE_IS_MISSING = -10;
        public static final int PHONE_FORMAT_ERROR = -11;
        public static final int PASSWORD_IS_MISSING = -12;
        public static final int PASSWORD_NOT_CORRECT = -13;
        public static final int PASSWORD_FORMAT_ERROR = -14;
        public static final int FILENAME_IS_MISSING = -15;
        public static final int QINIU_DELETE_FAILED = -16;
        public static final int WEIBO_UID_IS_MISSING = -17;
        public static final int WEIBO_UID_FORMAT_ERROR = -18;
        public static final int WEIBO_UID_EXISTS = -19;
        public static final int ACCOUNT_EXISTS = -20;
        public static final int ACCOUNT_NOT_AVAILABLE = -21;
        public static final int ACCOUNT_ID_IS_MISSING = -22;
        public static final int ACCOUNT_DOESNT_EXIST = -23;
        public static final int WEIXIN_UID_IS_MISSING = -24;
        public static final int WEIXIN_UID_FORMAT_ERROR = -25;
        public static final int WEIXIN_UID_IS_EXISTS = -26;
        public static final int PHONE_VERIFIED = -27;
        public static final int PHONE_VERIFY_CODE_IS_MISSING = -28;
        public static final int PHONE_VERIFY_CODE_WRONG = -29;
        public static final int SMS_SEND_FAIL = -30;
        public static final int NICKNAME_FORMAT_ERROR = -32;
        public static final int DESCRIPTION_FORMAT_ERROR = -33;
        public static final int GENDER_FORMAT_ERROR = -34;
        public static final int LOCATION_FORMAT_ERROR = -35;
        public static final int AVATAR_URL_ERROR = -36;
        public static final int NICKNAME_EXISTS = -37;
        public static final int PHONE_NOT_VERIFIED = -38;
        public static final int WEIBO_UID_NOT_MATCH = -39;
        public static final int WEIXIN_UID_NOT_MATCH = -40;
        public static final int IOS_DEVICE_TOKEN_IS_MISSING = -41;
        public static final int ADMIN_DYNAMIC_PASSWORD_MISSING = -42;
        public static final int CAPTCHA_IS_MISSING = -43;
        public static final int UNKNOWN_ERROR = -44;
        public static final int DYNAMIC_PW_EXPIRED = -45;
        public static final int ACCOUNT_AUTH_ERROR = -46;
        public static final int WORLD_ID_MISSING = -47;
        public static final int WORLD_DESCRIPTION_MISSING = -48;
        public static final int WORLD_DOESNT_EXIST = -49;
        public static final int WORLD_LIKED = -50;
        public static final int CAPTCHA_ERROR = -51;
        public static final int REPORT_REASON_MISSING = -52;
        public static final int BANNER_IMAGE_URL_MISSING = -53;
        public static final int BANNER_HREF_MISSING = -54;
        public static final int BANNER_ID_MISSING = -55;
        public static final int NICKNAME_EMPTY = -56;
        public static final int REPORT_ID_MISSING = -57;
        public static final int WORLD_REPORT_SOLUTION_MISSING = -58;
        public static final int REPORT_REASON_EMPTY = -59;
        public static final int REPORT_HANDLED = -60;
        public static final int BANNER_TITLE_MISSING = -61;
        public static final int EXPORT_USERS_INFO_START_DATE_MISSING = -62;
        public static final int EXPORT_USERS_INFO_END_DATE_MISSING = -63;
        public static final int BANNER_DOESNT_EXIST = -64;
        public static final int RANKING_MISSING = -65;
        public static final int WORLD_NOT_AVALIABLE = -66;
        public static final int RANKING_BASE_MISSING = -67;
        public static final int WORLD_VIDEO_ID_MISSING = -68;
        public static final int NICKNAME_MISSING = -69;
        public static final int SMS_NOT_SENT = -70;
        public static final int LIMIT_FORMAT_ERROR = -71;
        public static final int TYPE_FORMAT_ERROR = -72;
        public static final int ACCOUNT_ID_FORMAT_ERROR = -73;
        public static final int PHONE_VERIFY_CODE_EXPIRES = -74;
        public static final int GENDER_MISSING = -75;
        public static final int AVATAR_URL_MISSING = -76;
        public static final int LOCATION_MISSING = -77;
        public static final int FILENAME_FORMAT_ERROR = -78;
        public static final int TYPE_MISSING = -79;
        public static final int TAG_MISSING = -80;
        public static final int TAG_FORMAT_ERROR = -81;
        public static final int WORLD_VIDEO_ID_FORMAT_ERROR = -82;
        public static final int WORLD_VIDEO_DOESNT_EXIST = -83;
        public static final int TAG_DOESNT_EXIST = -84;
        public static final int DAILY_TAG_FILE_DOESNT_EXIST = -85;
        public static final int CHECK_FORMAT_ERROR = -86;
        public static final int SEED_FORMAT_ERROR = -87;
        public static final int OFFSET_FORMAT_ERROR = -88;
        public static final int WORDS_MISSING = -89;
        public static final int WORDS_FORMAT_ERROR = -90;
        public static final int TAG_TYPE_FORMAT_ERROR = -91;
        public static final int VIDEO_TYPE_FORMAT_ERROR = -92;
        public static final int SOCIAL_UID_MISSING = -93;
        public static final int QQ_UID_MISSING = -94;
        public static final int QQ_EXISTS = -95;
        public static final int QQ_NUMBER_MISSING = -96;
        public static final int QQ_NICKNAME_MISSING = -97;
        public static final int WEIBO_NICKNAME_MISSING = -98;
        public static final int WEIBO_NICKNAME_FORMAT_ERROR = -99;
        public static final int WEIXIN_NICKNAME_MISSING = -100;
        public static final int WEIXIN_NICKNAME_FORMAT_ERROR = -101;
        public static final int QQ_NUMBER_FORMAT_ERROR = -102;
        public static final int QQ_NICKNAME_FORMAT_ERROR = -103;
        public static final int QQ_NUMBER_NOT_MATCH = -104;
    }

    public static class UmengData {
        public static final String FAB_WORLD_CLICK = "fab_world_click";
        public static final String FAB_DIARY_CLICK = "fab_diary_click";

    }
}
