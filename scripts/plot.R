list.of.packages <- c("ggplot2", "readr")
new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages)

suppressPackageStartupMessages(library(ggplot2))
suppressPackageStartupMessages(library(readr))

raw = read_csv(file = "./test.csv",col_names = c("thpt","mammoth"))

p1 = ggplot(data = raw, aes(x = 1:nrow(raw), y = thpt)) +
  geom_line() + xlab("secs") + ylab("throughput (txn/s)") + 
   theme_bw()
p1 = p1 + geom_vline(xintercept = which(raw$mammoth == 1),color = "red")

ggsave("./plot.png", p1, width = 8, height = 6, device = "png")
