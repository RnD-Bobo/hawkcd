/*
 * Copyright (C) 2016 R&D Solutions Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.hawkcd.agent.models.payload;

public class EnvironmentInfo {
    private String osName;
    private String osVersion;
    private String hostName;
    private double totalRamMBytes;
    private double freeRamMBytes;
    private long totalDiskSpaceGBytes;
    private long freeDiskSpaceGBytes;

    public EnvironmentInfo() {
        // Init();
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public double getTotalRamMBytes() {
        return totalRamMBytes;
    }

    public void setTotalRamMBytes(double totalRamMBytes) {
        this.totalRamMBytes = totalRamMBytes;
    }

    public double getFreeRamMBytes() {
        return freeRamMBytes;
    }

    public void setFreeRamMBytes(double freeRamMBytes) {
        this.freeRamMBytes = freeRamMBytes;
    }

    public long getTotalDiskSpaceGBytes() {
        return totalDiskSpaceGBytes;
    }

    public void setTotalDiskSpaceGBytes(long totalDiskSpaceGBytes) {
        this.totalDiskSpaceGBytes = totalDiskSpaceGBytes;
    }

    public long getFreeDiskSpaceGBytes() {
        return freeDiskSpaceGBytes;
    }

    public void setFreeDiskSpaceGBytes(long freeDiskSpaceGBytes) {
        this.freeDiskSpaceGBytes = freeDiskSpaceGBytes;
    }

//    private void Init() {
//        //Disk Space
//        long totalSize = 0;
//        long freeSpace = 0;
//
////        foreach (DriveInfo drive in DriveInfo.GetDrives())
////        {
////
////            totalSize += drive.TotalSize;
////            freeSpace += drive.TotalFreeSpace;
////        }
//
//
//        this.totalDiskSpaceGBytes = totalSize / 1024 / 1024 / 1024;
//        this.freeDiskSpaceGBytes = freeSpace / 1024 / 1024 / 1024;
//
//        //Ram
//        this.totalRamMBytes = GetTotalRam();
//        this.freeRamMBytes = GetFreeRam();
//        //TODO: fix this figure how this works on nix OS
//        this.hostName = "fixme"; //System.Environment.GetEnvironmentVariables()["nothing"]; //System.Environment.MachineName;
//    }


////    public class MEMORYSTATUSEX
////    {
////        public uint dwLength;
////        public uint dwMemoryLoad;
////        public ulong ullTotalPhys;
////        public ulong ullAvailPhys;
////        public ulong ullTotalPageFile;
////        public ulong ullAvailPageFile;
////        public ulong ullTotalVirtual;
////        public ulong ullAvailVirtual;
////        public ulong ullAvailExtendedVirtual;
////        public MEMORYSTATUSEX()
////        {
////            this.dwLength = 1; //(uint)Marshal.SizeOf(typeof(MEMORYSTATUSEX));
////        }
////    }
//
//    //[DllImport("kernel32.dll", CharSet = CharSet.Auto, SetLastError = true)]
//    //public static extern bool GlobalMemoryStatusEx([In, Out] MEMORYSTATUSEX lpBuffer);
//
//    public static long GetTotalRam() {
//        //TODO: implement
//        //MEMORYSTATUSEX mem = new MEMORYSTATUSEX();
//
//        //GlobalMemoryStatusEx(mem);
//
//        //return mem.ullTotalPhys;
//        return 1;
//    }
//
//    public static long GetFreeRam() {
//        //TODO: Implement
//        //MEMORYSTATUSEX mem = new MEMORYSTATUSEX();
//
//        //GlobalMemoryStatusEx(mem);
//
//        //return mem.ullAvailPhys;
//        return 1;
//    }
}
