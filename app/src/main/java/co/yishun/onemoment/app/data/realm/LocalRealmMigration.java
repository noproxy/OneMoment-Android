package co.yishun.onemoment.app.data.realm;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;

/**
 * Created by Jinge on 2016/1/6.
 */
public class LocalRealmMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm dynamicRealm, long l, long l1) {
        RealmObjectSchema database = dynamicRealm.getSchema().get("OMDataBase");
        database.setRequired("createTime", true);
        database.setRequired("updateTime", true);
        RealmObjectSchema tagData = dynamicRealm.getSchema().get("OMLocalVideoTag");
        tagData.setRequired("tagDate", true);
        tagData.setRequired("tagText", true);
        tagData.setRequired("tagPosition", true);
    }
}
