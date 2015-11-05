import numpy as np
from glob import glob
import scipy.stats


def mean_confidence_interval(sample, confidence=0.95):
    a = 1.0*np.array(sample)
    n = len(a)
    m, se = np.mean(a), scipy.stats.sem(a)
    h = se * scipy.stats.t.ppf((1+confidence)/2., n-1)
    return m-h, m+h

file_names = glob('data/*.log')
data = np.concatenate([np.loadtxt(open(f, "rb"), delimiter=" ") for f in file_names])
throughput = np.array([x[1] for x in data if x[0] == -1])
throughput.sort()
data = np.array([x[1] for x in data if x[0] != -1])
data.sort()
# print("Throughputs: " + str(throughput))
# print("Data: " + str(data))
print("Median: " + str(np.median(data)))
print("Mean: " + str(data.mean()))
print("Standard deviation: " + str(np.std(data, ddof=1)))
print("Variance: " + str(data.var(ddof=1)))
print("Throughput: " + str(np.sum(throughput)))
lower, upper = mean_confidence_interval(data)
print("95% Confidence interval: [" + str(lower) + ", " + str(upper) + "]")
lower, upper = mean_confidence_interval(data, 0.96)
print("96% Confidence interval: [" + str(lower) + ", " + str(upper) + "]")
lower, upper = mean_confidence_interval(data, 0.97)
print("97% Confidence interval: [" + str(lower) + ", " + str(upper) + "]")
lower, upper = mean_confidence_interval(data, 0.98)
print("98% Confidence interval: [" + str(lower) + ", " + str(upper) + "]")
lower, upper = mean_confidence_interval(data, 0.99)
print("99% Confidence interval: [" + str(lower) + ", " + str(upper) + "]")
print("")
mean = data.mean()
print("3% interval around the mean: [" + str(mean-0.03*mean) + ", " + str(mean+0.03*mean) + "]")
print("4% interval around the mean: [" + str(mean-0.04*mean) + ", " + str(mean+0.04*mean) + "]")
print("5% interval around the mean: [" + str(mean-0.05*mean) + ", " + str(mean+0.05*mean) + "]")
print("7.5% interval around the mean: [" + str(mean-0.075*mean) + ", " + str(mean+0.075*mean) + "]")
print("10% interval around the mean: [" + str(mean-0.1*mean) + ", " + str(mean+0.1*mean) + "]")
