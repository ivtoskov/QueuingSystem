import numpy as np
from glob import glob
from scipy.stats import itemfreq

file_names = glob('data/*.log')
data = np.concatenate([np.loadtxt(open(f, "rb"), delimiter=" ") for f in file_names])
totalThroughput = np.array([x[1] for x in data if x[0] == -1])
print("Total throughput: " + str(np.sum(totalThroughput)))
data = np.array([x for x in data if x[0] >= 0])

second = 60000
data[:, 0] = np.floor(data[:, 0]/second) + 1

bins = np.array([[y, np.mean(np.array([x[1] for x in data if x[0] == y]))] for y in range(1, 31)])
realThroughput = np.array(itemfreq(data[:, 0]))
realThroughput[:, 1] = realThroughput[:, 1]/60
print("Calculated throughput: " + str(np.sum(realThroughput[:, 1])/30))
np.savetxt("responseTime", bins, delimiter=" ")
np.savetxt("throughPut", realThroughput, delimiter=" ")
