rdr <- read.csv(file="r_dr.csv", head=FALSE, sep=",")

plot(
rdr$V1, 
rdr$V2, 
pch='`', 
ylab="Change in Normalized Ranking", 
xlab="Normalized Ranking")

lsfit <- lm(rdr$V2 ~ rdr$V1)

abline(lsfit, col="red", lwd=3)

lsfit$coefficients

summary(lsfit)
