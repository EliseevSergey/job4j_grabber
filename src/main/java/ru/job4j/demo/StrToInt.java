package ru.job4j.demo;

public class StrToInt {
    public static void main(String[] args) {
        String ten = "10";
        try {
            int i = Integer.parseInt(ten.trim());
            System.out.println(i);
        } catch (NumberFormatException nfe) {
            System.out.println("Number format exception: " + nfe.getMessage());
        }
    }
}
