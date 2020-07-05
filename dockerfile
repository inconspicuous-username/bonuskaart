FROM golang:latest AS builder
WORKDIR /go/src/BonusKaartApi/
RUN go get -d -v golang.org/x/net/html  
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o main .

FROM alpine:latest  
RUN apk --no-cache add ca-certificates
WORKDIR /root/
COPY --from=builder /go/src/BonusKaartApi/ .
copy bonuskaart.txt .
CMD ["./main"]  
