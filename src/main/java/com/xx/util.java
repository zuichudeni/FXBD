package com.xx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.Stack;

//临时工具
public class util {
    public static void main() throws IOException {
        toBDIcon();
    }

    private static void transfer(String dirFrom, String dirTo) throws IOException {
        File fileFrom = new File(dirFrom);
        File fileTo = new File(dirTo);
        Stack<File> stack = new Stack<>();
        stack.push(fileFrom);
        while (!stack.isEmpty()) {
            File currentFile = stack.pop();
            if (currentFile.isFile()) {
                String fileName = currentFile.getName();
                if (fileName.endsWith(".svg")) {
                    File newFile = new File(fileTo, fileName);
                    if (!newFile.exists())
                        Files.copy(currentFile.toPath(), newFile.toPath());
                }
            } else {
                for (File child : Objects.requireNonNull(currentFile.listFiles()))
                    stack.push(child);
            }
        }
    }

    private static void toBDIcon() {
        File file = new File("F:\\project\\java\\FXEditor\\src\\main\\resources\\texture");
        Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(image -> {
            String name = image.getName();
            System.out.printf("%s(\"/texture/%s\"),\n", convertToConstantCase(name.substring(0, name.length() - 4)), image.getName());
        });
    }

    public static String convertToConstantCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean lastCharWasLower = false;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            // 遇到大写字母且前一个字符是小写字母
            if (Character.isUpperCase(currentChar) && lastCharWasLower) {
                result.append('_');
            }

            // 添加当前字符（转换为大写）
            result.append(Character.toUpperCase(currentChar));

            // 检查当前字符是否为小写字母
            lastCharWasLower = Character.isLowerCase(currentChar);
        }

        return result.toString();
    }
}
