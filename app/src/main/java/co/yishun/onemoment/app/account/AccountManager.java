package co.yishun.onemoment.app.account;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.util.GsonFactory;

/**
 * Created by Carlos on 2015/8/13.
 */
public class AccountManager {
    public static final String ACCOUNT_TYPE = "co.yishun.onemoment.app";
    public static final String ACCOUNT_ID_KEY = "id";
    private static final String TAG = "AccountManager";
    public static android.accounts.AccountManager accountManager;
    public static Account account;
    private static User mUser = null;
    private static HashMap<Integer, OnUserInfoChangeListener> mListeners = new HashMap<>();

    private static android.accounts.AccountManager getAccountManager(Context context) {
        if (accountManager == null) {
            accountManager = (android.accounts.AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        }
        return accountManager;
    }

    protected static Account getAccount(Context context) {
        if (account == null) {
            Account[] accounts = getAccountManager(context).getAccountsByType(ACCOUNT_TYPE);
            if (accounts.length >= 1) {
                account = accounts[0];
            }
        }
        return account;
    }

    public static boolean isLogin(Context context) {
        context = context.getApplicationContext();
        if (getAccount(context) == null) return false;
        else if (getUserInfo(context) == null) {
            deleteAccount(context);
            return false;
        }
        return true;
    }

    public static void saveAccount(Context context, User user) {
        context = context.getApplicationContext();
        Account newAccount = new Account(user.nickname, ACCOUNT_TYPE);
        Bundle bundle = new Bundle();
        bundle.putString(ACCOUNT_ID_KEY, user._id);
        getAccountManager(context).addAccountExplicitly(newAccount, null, bundle);
        updateOrCreateUserInfo(context, user);
        SyncManager.notifySyncSettingsChange(context);
    }

    public static String getAccountId(Context context) {
        context = context.getApplicationContext();
        account = getAccount(context);
        return getAccountManager(context).getUserData(account, ACCOUNT_ID_KEY);
    }

    public static void deleteAccount(Context context) {
        context = context.getApplicationContext();
        account = getAccount(context);
        mUser = null;
        deleteUserInfo(context);
        getAccountManager(context).removeAccount(account, null, null);
        SyncManager.disableSync();

        account = null;
    }

    /**
     * delete account create in version 1.x because {@link Account#name} of account in 1.x equals
     * {@link User#_id}, while {@link Account#name} of account in 1.x equals {@link User#nickname}.
     */
    public static void deleteOldAccount(Context context, User user) {
        context = context.getApplicationContext();
        Account[] accounts = getAccountManager(context).getAccountsByType(ACCOUNT_TYPE);
        for (Account a : accounts) {
            if (TextUtils.equals(a.name, user._id)) {
                getAccountManager(context).removeAccount(a, null, null);
                break;
            }
        }
        account = null;
    }

    public static boolean updateOrCreateUserInfo(Context context, User user) {
        context = context.getApplicationContext();
        deleteUserInfo(context);
        return saveUserInfo(context, user);
    }

    private static boolean saveUserInfo(Context context, User user) {
        try {
            String path = getUserInfoDir(context);
            LogUtil.i(TAG, "save identity info, path: " + path);
            mUser = user;
            String userJson = GsonFactory.newNamingGson().toJson(mUser);
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(userJson);
            fileWriter.flush();
            fileWriter.close();
            onUserInfoChange(user);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static void onUserInfoChange(User user) {
        for (OnUserInfoChangeListener l : mListeners.values()) {
            l.onUserInfoChange(user);
        }
    }

    private static String getUserInfoDir(Context context) {
        return context.getDir(Constants.IDENTITY_DIR, Context.MODE_PRIVATE) + "/" + Constants.IDENTITY_INFO_FILE_NAME;
    }

    private static void loadUserInfo(Context context) {
        try {
            String path = getUserInfoDir(context);
            LogUtil.i(TAG, "load identity info, path: " + path);
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(path);
                mUser = GsonFactory.newNamingGson().fromJson(fileReader, User.class);
            } catch (JsonSyntaxException | JsonIOException e) {
                LogUtil.i(TAG, "user info need update");
            } finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteUserInfo(Context context) {
        String path = context.getDir(Constants.IDENTITY_DIR, Context.MODE_PRIVATE) + "/" + Constants.IDENTITY_INFO_FILE_NAME;
        File info = new File(path);
        if (info.exists() && info.delete())
            mUser = null;
    }

    public static User getUserInfo(Context context) {
        //TODO user info file check and download
        context = context.getApplicationContext();
        if (mUser == null) {
            loadUserInfo(context.getApplicationContext());
        }
        return mUser;
    }

    public static void addOnUserInfoChangedListener(@NonNull OnUserInfoChangeListener listener) {
        mListeners.put(listener.hashCode(), listener);
    }

    public static void removeOnUserInfoChangedListener(@NonNull OnUserInfoChangeListener listener) {
        mListeners.remove(listener.hashCode());
    }


    public interface OnUserInfoChangeListener {
        void onUserInfoChange(User info);
    }
}
