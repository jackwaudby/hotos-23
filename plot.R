suppressPackageStartupMessages(library(ggplot2))
suppressPackageStartupMessages(library(readr))


raw = read_csv(file = "test.csv",col_names = c("thpt","mammoth"))
raw
plot(1:nrow(raw),raw$thpt,type = "l",xlab = "secs",ylab="thpt")
abline(v=which(raw$mammoth == 1))
