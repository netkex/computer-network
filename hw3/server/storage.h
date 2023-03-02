#pragma once
#include <fstream>
#include <streambuf>
#include <unistd.h>

struct FileStorage {
    FileStorage() {} 

    std::string get(const std::string& file_name) {
        if (access(file_name.c_str(), F_OK) == -1) {
            throw std::runtime_error("file does not exist");
        }
        
        std::ifstream file(file_name);
        std::string file_content((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());

        if (file_content.empty()) {
            throw std::runtime_error("file is empty");
        }

        return file_content;
    }
};
