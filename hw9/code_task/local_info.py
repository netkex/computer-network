import netifaces

netinfo = netifaces.ifaddresses('en0')[netifaces.AF_INET][0]
ip_address = netinfo['addr']
net_mask = netinfo['netmask']

print(f"IP address: {ip_address}")
print(f"Network mask: {net_mask}")
