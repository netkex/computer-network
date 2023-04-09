package main

import (
	"bytes"
	"fmt"
	"math/rand"
	"net"
	"os"
	"strconv"
	"strings"
	"time"
)

func sendResponse(addr net.Addr, updServer net.PacketConn, buf []byte, history map[string]time.Time) {
	message := string(bytes.Trim(buf, "\x00"))
	fmt.Printf("message %s; from: %s\n", message, addr.String())

	if rand.Float32() <= 0.2 {
		fmt.Println("simulate package loss to", addr.String())
		return
	}

	// workaround to have same time zone everywhere
	curTime, _ := time.Parse("2006-01-02T15:04:05.000Z", time.Now().Format("2006-01-02T15:04:05.000Z"))
	var (
		seq        int
		timeString string
	)

	if _, err := fmt.Sscanf(message, "%d %s", &seq, &timeString); err != nil {
		fmt.Println("failed to parse message from", addr.String())
		return
	}

	messageTime, err := time.Parse("2006-01-02T15:04:05.000Z", timeString)
	if err != nil {
		fmt.Println("failed to parse message from", addr.String())
		return
	}

	fmt.Printf("time diff: %v\n", curTime.Sub(messageTime))
	if prevTime, ok := history[addr.String()]; ok {
		fmt.Printf("time from last message: %v\n", time.Now().Sub(prevTime))
	}

	history[addr.String()] = time.Now()

	feedbackMessage := strings.ToUpper(message)
	updServer.WriteTo([]byte(feedbackMessage), addr)
}

func checkServices(history map[string]time.Time, timeOut time.Duration) {
	curTime := time.Now()
	var servicesToDelete []string

	for service, lastMessageTime := range history {
		if curTime.Sub(lastMessageTime) > timeOut {
			servicesToDelete = append(servicesToDelete, service)
		}
	}

	for _, service := range servicesToDelete {
		fmt.Printf("service %s was stopped\n", service)
		delete(history, service)
	}
}

func main() {
	port, err := strconv.Atoi(os.Args[1])
	if err != nil {
		fmt.Println("incorrect server port")
		return
	}

	serviceTimeOutInSeconds, err := strconv.Atoi(os.Args[2])
	if err != nil {
		fmt.Println("incorrect timeout")
		return
	}

	serviceTimeout := time.Duration(serviceTimeOutInSeconds) * time.Second

	udpServer, err := net.ListenPacket("udp", ":"+strconv.Itoa(port))
	if err != nil {
		fmt.Println("failed to init upd server")
		return
	}
	defer udpServer.Close()

	history := make(map[string]time.Time)

	for {
		checkServices(history, serviceTimeout)

		buf := make([]byte, 1024)
		if udpServer.SetReadDeadline(time.Now().Add(serviceTimeout)) != nil {
			continue
		}

		_, addr, err := udpServer.ReadFrom(buf)
		if err != nil {
			continue
		}

		sendResponse(addr, udpServer, buf, history)
	}
}
