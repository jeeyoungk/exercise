package com.kimjeeyoung.lang;

/**
 * Experimentation with java's private accessors.
 */
public class PrivateMember {
    static class Parent {
        private void privateMethod() {
            System.out.println("Hello!");
        }
    }

    static class Child extends Parent {
        void wrapperMethod() {
            super.privateMethod();
            // privateMethod(); // <- this line errors, why?
        }
    }


    public static void main(String ... args) {
        Child child = new Child();
        child.wrapperMethod();
        Parent parent = new Parent();
        parent.privateMethod();
    }
}