package main

import (
	"fmt"
	"net"
	"os"
	"strconv"
	"time"
)

func main() {
	serverEndpoint := os.Args[1]

	udpServerAddr, err := net.ResolveUDPAddr("udp", serverEndpoint)
	if err != nil {
		fmt.Println("incorrect server end point")
		return
	}

	udpClient, err := net.ListenPacket("udp", ":0")
	if err != nil {
		fmt.Println("failed to init upd client")
		return
	}
	defer udpClient.Close()

	layout := "2006-01-02T15:04:05.000Z"

	loss, all, minRTT, maxRTT, avgRTT := 0, 0, time.Duration(0), time.Duration(0), time.Duration(0)
	for i := 0; i < 10; i++ {
		for {
			t1 := time.Now()
			udpClient.WriteTo([]byte(strconv.Itoa(i+1)+" "+t1.Format(layout)), udpServerAddr)

			buf := make([]byte, 1024)
			if udpClient.SetReadDeadline(time.Now().Add(time.Second)) != nil {
				fmt.Println("failed to set read deadline")
				continue
			}

			all += 1
			bytesLen, _, err := udpClient.ReadFrom(buf)
			if err != nil {
				fmt.Println("Request timed out")
				loss += 1
				continue
			}

			t2 := time.Now()
			fmt.Println("response: ", string(buf))

			RTT := t2.Sub(t1)
			avgRTT += RTT

			if minRTT == 0 || minRTT > RTT {
				minRTT = RTT
			}
			if maxRTT == 0 || maxRTT < RTT {
				maxRTT = RTT
			}

			fmt.Printf("%d bytes from %s: icmp_seq=%d, time=%v\n", bytesLen, udpServerAddr.String(), i+1, RTT)
			break
		}
	}

	avgRTT /= 10
	fmt.Printf("%d packets transmitted, %d packets received, %f %% packet loss\n", all, all-loss, float32(loss)/float32(all)*100)
	fmt.Println("round-trip min/avg/max =", minRTT, "/", avgRTT, "/", maxRTT)
}
