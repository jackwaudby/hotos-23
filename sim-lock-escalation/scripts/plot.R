suppressPackageStartupMessages(library(ggplot2))
suppressPackageStartupMessages(library(readr))
suppressPackageStartupMessages(library(dplyr))

raw = read_csv(file = "./results.csv",col_names = TRUE,show_col_types = FALSE)

range = raw %>% group_by(communities) %>%
  dplyr::summarize(rangeLocked = median(rangeLocked))

community = raw %>% group_by(communities) %>%
  dplyr::summarize(communityLocked = median(communityLocked))

raw = left_join(range, community, by = join_by(communities == communities))

p1 = ggplot(data = raw, aes(x = communities)) +
  geom_line(aes(y = rangeLocked, color = "red")) +
  geom_line(aes(y = communityLocked, color = "black")) +
  xlab("communities") + ylab("keys locked") + 
  scale_colour_manual(name = '', values =c('black'='black','red'='red'), labels = c('community','range')) +
  theme_bw() 

ggsave("lock-sim.png", p1, width = 8, height = 6, device = "png")
