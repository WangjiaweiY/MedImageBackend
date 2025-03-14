package com.nwu.medimagebackend.common;

public class FileItem {
        private String name;
        private boolean directory; // true 表示目录，false 表示文件

        public FileItem(String name, boolean directory) {
            this.name = name;
            this.directory = directory;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isDirectory() {
            return directory;
        }

        public void setDirectory(boolean directory) {
            this.directory = directory;
        }
    }