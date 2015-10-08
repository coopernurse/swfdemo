package com.imprev.swf;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public interface Activity {

    String getActivityName();
    ActivityResult execute(String input);

}
