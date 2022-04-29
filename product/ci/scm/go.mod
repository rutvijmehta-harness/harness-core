module github.com/rutvijmehta-harness/harness-core/product/ci/scm

go 1.14

replace github.com/harness/harness-core/commons/go/lib => ../../../commons/go/lib

replace github.com/harness/harness-core/product/log-service => ../../../product/log-service

replace github.com/harness/harness-core/product/ci/scm => github.com/rutvijmehta-harness/harness-core/product/ci/scm

require (
	github.com/alexflint/go-arg v1.3.0
	github.com/drone/go-scm v1.20.1-0.20220404090931-cc4578ad43c3
	github.com/drone/go-scm-codecommit v0.0.0-20210315104920-2d8b9dc5ed8a
	github.com/golang/mock v1.6.0
	github.com/golang/protobuf v1.5.2
	github.com/grpc-ecosystem/go-grpc-middleware v1.2.1
	github.com/harness/harness-core/commons/go/lib v0.0.0-20220222141117-7659b7eca599
	github.com/stretchr/testify v1.7.0
	go.uber.org/zap v1.15.0
	golang.org/x/net v0.0.0-20220114011407-0dd24b26b47d
	google.golang.org/grpc v1.43.0
	google.golang.org/protobuf v1.27.1
	gopkg.in/yaml.v3 v3.0.0-20210107192922-496545a6307b // indirect
	mvdan.cc/sh/v3 v3.2.4 // indirect
)
