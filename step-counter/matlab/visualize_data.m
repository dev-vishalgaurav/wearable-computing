close all;

% exponential average for different choices of alpha
alpha_list = [0 0.01 0.1 0.3 0.6 0.9];
smoothingFactor = 4;

acc_raw = csvread('acc_raw_stable.csv');
x = acc_raw(:, 2);
y = acc_raw(:, 3);
z = acc_raw(:, 4);

start_idx = 1000;
sample_num = 3000;

%get a short window from the raw data
%crop = acc_raw(start_idx:start_idx + sample_num-1, :);
crop = acc_raw;

amp = sqrt(sum(crop(:,2:end).^2,2)); % get the amplitude of the signal

% a(:,1) = expsmooth(crop(:,2),alpha_list(5));
% a(:,2) = expsmooth(crop(:,3),alpha_list(5));
% a(:,3) = expsmooth(crop(:,4),alpha_list(5));
% amp = sqrt(sum(a.^2,2)); % get the amplitude of the signal

figure
plot(x); hold on; 
plot(y); hold on; 
plot(z); hold on; 
legend('x','y','z');

num_figure = 5;
subplot(num_figure,1,1)
plot(crop(:,2));
title('Accelerometer (x)');

subplot(num_figure,1,2)
plot(crop(:,3));
title('Accelerometer (y)');

subplot(num_figure,1,3)
plot(crop(:,4));
title('Accelerometer (z)');

subplot(num_figure,1,4)
plot(amp);
title('Accelerometer (m)');

figure
subplot(3,2,1);
plot(amp);
title('original mag')

for i = 2:length(alpha_list)
    subplot(3,2,i);
    set(gca,'FontSize',15);
    plot(expsmooth(amp,alpha_list(i)),'g'); 
    title(sprintf('alpha = %0.1f',alpha_list(i)),'FontSize',16);
end

ampS = expsmooth(amp,alpha_list(smoothingFactor));
ampS = ampS(2:end);

figure
plot(ampS);
hold on;

samplingRate = 50; %50 samples per second
maxStepsPerSec = 5; %Only allow <=5 steps per second
wLength = 1*samplingRate; %3 seconds (50 samples per second)
numWindows = ceil(length(ampS)/wLength);
steps = 0;
zeroCrossings = [];
allZeroCrossings = [];
for i = 1:numWindows
    startInd = (i-1)*wLength + 1;
    endInd = startInd+wLength-1;
    if(endInd > length(ampS))
        endInd = length(ampS);
    end
    windAmp = ampS(startInd:endInd);
    Min = min(windAmp);
    Max = max(windAmp);
    thres = Min + (Max-Min)/2;
    
    line([startInd endInd],[Min Min], 'Color', 'b'); hold on; % plot the min line
    line([startInd endInd],[Max Max], 'Color', 'b'); hold on; % plot the max line
    
    line([startInd endInd],[thres thres], 'Color', 'r'); hold on; % plot the threshold line
    zeroCrossings = startInd + strfind((windAmp > thres), [1 0]); %These are the zero crossings where the signal FALLS BELOW the threshold   
    
    indRemove = [];
    for i = 1:length(zeroCrossings)-1
        if (abs(zeroCrossings(i+1)-zeroCrossings(i))) < (samplingRate/maxStepsPerSec)
            indRemove = [indRemove i i+1];
        end
    end
    disp(['Remove ' num2str(unique(zeroCrossings(indRemove)))])
    % ONLY KEEP ZERO CROSSINGS WHICH ARE GREATER THAT > samplingRate/maxStepsPerSec
    indKeep = setdiff([1:length(zeroCrossings)], indRemove);
    zeroCrossings = zeroCrossings(indKeep);
    
    % ONLY KEEP ZERO CROSSINGS WHICH ARE GREATER THAT > samplingRate/maxStepsPerSec
    indKeep = setdiff([1:length(zeroCrossings)], indRemove);
    zeroCrossings = zeroCrossings(indKeep);
    
    if(length(zeroCrossings)>=maxStepsPerSec || ((Max - Min)<1.5)) %if there are more than max allowable steps per second, then the steps are not valid
        zeroCrossings = [];
    end
    allZeroCrossings = [allZeroCrossings zeroCrossings];
    steps = steps + length(zeroCrossings);
end

plot(allZeroCrossings, ampS(allZeroCrossings), 'x'); hold on;

title(sprintf('Number of steps = %i', steps));


