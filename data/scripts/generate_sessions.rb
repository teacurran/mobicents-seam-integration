#!/usr/bin/ruby

(1000..9999).each do |i|
	puts "INSERT INTO `session` (`id`, dateCreated, dateValidated, callId) VALUES(" << i.to_s() << ", null, null, null);"
end
