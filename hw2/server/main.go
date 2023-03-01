package main

import (
	"fmt"
	"os"
	"server/server"
	"strconv"
)

func main() {
	port, err := strconv.Atoi(os.Args[1])
	if err != nil {
		fmt.Println("incorrect port")
		return
	}

	s := server.NewServer(port)
	s.Start()
}
