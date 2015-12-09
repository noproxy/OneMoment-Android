package co.yishun.onemoment.app.data;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.model.OMDataBase;
import co.yishun.onemoment.app.data.model.OMLocalVideoTag;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created on 2015/10/27.
 */
public class RealmHelper {
    public static void setup(Context context) {
        String userId = AccountHelper.getUserInfo(context)._id;
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .name("tag-" + userId + ".realm").build();
        Realm.setDefaultConfiguration(config);

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        if (realm.where(OMDataBase.class).findAll().size() == 0) {
            OMDataBase dataBase = realm.createObject(OMDataBase.class);
            String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault())
                    .format(new Date()) +
                    "-" + Util.unixTimeStamp();
            dataBase.setCreateTime(time);
            dataBase.setUpdateTime(time);
        }
        realm.commitTransaction();
    }

    public static void addTodayTag(String text, float x, float y) {
        addTag(new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault())
                .format(new Date()), text, String.valueOf(x) + " " + String.valueOf(y));
    }

    public static void addTag(String date, String text, float x, float y) {
        addTag(date, text, String.valueOf(x) + " " + String.valueOf(y));
    }

    public static void addTag(String date, String text, String position) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        OMLocalVideoTag tag = realm.createObject(OMLocalVideoTag.class);
        tag.setTagDate(date);
        tag.setTagText(text);
        tag.setTagPosition(position);

        realm.where(OMDataBase.class).findFirst()
                .setUpdateTime(new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(new Date())
                        + Constants.URL_HYPHEN + Util.unixTimeStamp());
        realm.commitTransaction();
    }

    public static int getTagNum(String date) {
        return getTags(date).size();
    }

    public static RealmResults<OMLocalVideoTag> getTags(String date) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(OMLocalVideoTag.class).equalTo("tagDate", date).findAll();
    }

    public static void removeTodayTags(){
        removeTags(new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(new Date()));
    }

    public static void removeTags(String date){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        getTags(date).clear();
        realm.commitTransaction();
    }
}
