package co.yishun.onemoment.app.account;

import android.accounts.Account;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;

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
        if (getAccount(context) == null) return false;
        else if (getUserInfo(context) == null){
            deleteAccount(context);
            return false;
        }
        return true;
    }

    public static void saveAccount(Context context, User user) {
        Account newAccount = new Account(user.nickname, ACCOUNT_TYPE);
        Bundle bundle = new Bundle();
        bundle.putString(ACCOUNT_ID_KEY, user._id);
        getAccountManager(context).addAccountExplicitly(newAccount, null, bundle);
        updateOrCreateUserInfo(context, user);
        SyncManager.notifySyncSettingsChange(context);
    }

    public static String getAccountId(Context context) {
        account = getAccount(context);
        return getAccountManager(context).getUserData(account, ACCOUNT_ID_KEY);
    }

    public static boolean deleteAccount(Context context) {
        account = getAccount(context);
        mUser = null;
        deleteUserInfo(context);
        MyFuture future = new MyFuture();
        CountDownLatch latch = new CountDownLatch(1);
        getAccountManager(context).removeAccount(account, future, null);
        SyncManager.disableSync();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        account = null;
        return future.result;
    }

    public static boolean updateOrCreateUserInfo(Context context, User user) {
        deleteUserInfo(context);
        return saveUserInfo(context, user);
    }

    private static boolean saveUserInfo(Context context, User user) {
        try {
            String path = getUserInfoDir(context);
            Log.i(TAG, "save identity info, path: " + path);
            FileOutputStream fout = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(user);
            oos.close();
            mUser = user;
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
            FileInputStream fin = null;
            fin = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fin);
            mUser = (User) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteUserInfo(Context context) {
        String path = context.getDir(Constants.IDENTITY_DIR, Context.MODE_PRIVATE) + "/" + Constants.IDENTITY_INFO_FILE_NAME;
        File info = new File(path);
        if (info.exists() && info.delete())
            mUser = null;
    }

    public static User getUserInfo(Context con) {
        //TODO user info file check and download
        if (mUser == null) {
            loadUserInfo(con);
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

    private static class MyFuture implements AccountManagerCallback<Boolean> {
        boolean result = false;

        @Override
        public void run(AccountManagerFuture<Boolean> future) {
            try {
                result = future.getResult();
            } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                e.printStackTrace();
            }
        }
    }
}
