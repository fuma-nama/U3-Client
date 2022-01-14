package net.sonmoosans.u3.api;

import net.sonmoosans.u3.api.annotation.RequireToken;
import net.sonmoosans.u3.api.core.APICaller;
import net.sonmoosans.u3.api.core.Parameter;
import net.sonmoosans.u3.api.model.LoginEntry;
import net.sonmoosans.u3.api.model.Result;
import net.sonmoosans.u3.api.model.UserProfile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public class AccountAPI {

    /**Login to an account
     * @return account token**/
    public static Result<String> login(String email, String password) {
        return APICaller.callPOST(String.class, "account/login",
                new Parameter("email", email),
                new Parameter("password", password)
        );
    }

    /** Sign up an account
     * <br>Note: the email must be verified
     * @return Token of the new account
     * @see AccountAPI#sendVerifyCode
     * **/
    public static Result<String> signUp(String email, String username, String password, File avatar, String code) {
        return APICaller.callPOSTFile(String.class, avatar, "account",
                new Parameter("email", email),
                new Parameter("name", username),
                new Parameter("password", password),
                new Parameter("code", code)
        );
    }

    /** Sign up an account
     * <br>Note: the email must be verified
     * @return Token of the new account
     * @see AccountAPI#sendVerifyCode
     * **/
    public static Result<String> signUp(String email, String username, String password, String code) {
        return APICaller.callPOST(String.class, "account",
                new Parameter("email", email),
                new Parameter("name", username),
                new Parameter("password", password),
                new Parameter("code", code)
        );
    }

    /** Update user profile **/
    @RequireToken
    public static boolean updateUser(String username) {
        return APICaller.callPUT("account",
                new Parameter("name", username)
        );
    }

    /** Update user profile **/
    @RequireToken
    public static boolean updateUser(String username, File avatar) {
        return APICaller.callPUTFile(avatar, "account",
                new Parameter("name", username)
        );
    }

    /**
     * Delete an Account
     * <br>All account data will be deleted
     * <br>This action can't be redone
     * **/
    @RequireToken
    public static boolean deleteAccount() {
        return APICaller.callDELETE("account");
    }

    /**
     * Call this before doing http request that requires verify code
     * <br>To makes sure verify code is true
     * @return Is the input verify code true
     * @see AccountAPI#sendVerifyCode
     * **/
    public static Result<Boolean> isTrueVerifyCode(String email, String code) {
        return APICaller.callGET(Boolean.class, "account/verify/code",
                new Parameter("email", email),
                new Parameter("code", code)
        );
    }

    /**
     * Send verify code to existing email
     * <br>API call will be failed if email isn't exists
     * @return Success to call API
     * **/
    public static boolean sendVerifyCode(String email) {
        return APICaller.callPOST("account/verify",
                new Parameter("email", email)
        );
    }

    /**
     * Verify an email
     * <br>API call will be failed if email already exists
     * @return Success to call API
     * **/
    public static boolean verifyEmail(String email) {
        return APICaller.callPOST("account/verify/email",
                new Parameter("email", email)
        );
    }

    /**
     * Reset account password
     * @return Success to reset password**/
    public static boolean resetPassword(String email, String code, String newPassword) {
        return APICaller.callPOST("account/reset",
                new Parameter("email", email),
                new Parameter("new", newPassword),
                new Parameter("code", code)
        );
    }

    /**
     * Update account password
     * @apiNote This method requires token, use {@link AccountAPI#resetPassword(String, String, String)} if token is missing
     * @return Success to reset password**/
    @RequireToken
    public static boolean updatePassword(String oldPassword, String newPassword) {
        return APICaller.callPUT("account/login/password",
                new Parameter("old", oldPassword),
                new Parameter("new", newPassword)
        );
    }

    @RequireToken
    public static boolean updateEmail(String newEmail, String password, String code) {
        return APICaller.callPUT("account/login/email",
                new Parameter("email", newEmail),
                new Parameter("password", password),
                new Parameter("code", code)
        );
    }

    /**
     * Get User profile
     * <br>If user ID already exists in memory, get from memory
     * @return User profile, Null if user ID doesn't exist
     * @see Memory#getUser
     * **/
    @Nullable
    public static UserProfile getUser(int userID) {
        UserProfile user = Memory.getUser(userID);
        if (user == null) {
            user = APICaller.callGET(UserProfile.class, "account",
                    Parameter.intValue("id", userID)
            ).context();
            Memory.putUser(userID, user);
        }
        if (user != null)
            user.userID = userID;

        return user;
    }


    /**
     * Get User profile
     * <br>If user ID already exists in memory, get from memory
     * @return User profile, or {@link UserProfile#DELETED_USER} if user ID doesn't exist
     * @see AccountAPI#getUser
     * **/
    @Nonnull
    public static UserProfile getUserSafe(int userID) {
        UserProfile user = getUser(userID);

        if (user == null) {
            user = UserProfile.DELETED_USER;
        }
        return user;
    }

    /**
     * Get user ID from token
     * @return User ID, Null if token doesn't exist
     * **/
    public static Integer getUserID(String token) {
        return APICaller.callGET(Integer.class, "account/id",
                new Parameter("token", token)
        ).context();
    }

    @RequireToken
    public static Result<LoginEntry> getLoginEntry() {
        return APICaller.callGET(LoginEntry.class, "account/login");
    }
}
