data <- read.csv(file="CBBM/Team Rank/BrendanSucksPlot.txt", head=FALSE, sep=",")

g_range <- range(0, data$V2, data$V3, data$V4, data$V5)

# Graph autos using y axis that ranges from 0 to max 
# value in cars or trucks vector.  Turn off axes and 
# annotations (axis labels) so we can specify them ourself
plot(data$V2, type="o", col="blue", ylim=g_range, 
   axes=FALSE, ann=FALSE)

# Make x axis using year labels
axis(1, at=1:34, lab=data$V1)

# Make y axis with horizontal labels that display ticks at every 10 marks
axis(2, las=1, at=50*0:g_range[2])

# Create box around plot
box()

# Graph trucks with red dashed line and square points
lines(data$V3, type="o", pch=22, lty=1, col="red")
lines(data$V4, type="o", pch=4, lty=1, col="purple")
lines(data$V5, type="o", pch=8, lty=1, col="green")

# Label the x and y axes
title(xlab="Year")
title(ylab="Edge Inversions")

# Create a legend at (1, g_range[2]) that is slightly smaller 
# (cex) and uses the same line colors and points used by 
# the actual plots 
legend(1, g_range[2], c("Stochastic Heuristic","PageRank","FAS Approximation", "Win/Loss"), cex=0.8, 
   col=c("blue","red","purple","green"), pch=c(22,22,4,8), lty=c(1,1,1,1));