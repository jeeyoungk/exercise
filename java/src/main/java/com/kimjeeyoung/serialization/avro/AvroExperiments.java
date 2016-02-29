package com.kimjeeyoung.serialization.avro;

import com.kimjeeyoung.avro.User;

/**
 * Created by jeeyoungk on 2/28/16.
 */
public class AvroExperiments {
    public static void main (String ... args) {
        User user = new User();
        user.setFavoriteColor("red");
        user.setFavoriteNumber(10);
        user.setName("jeeyoung kim");
        System.out.println(user);
    }
}
