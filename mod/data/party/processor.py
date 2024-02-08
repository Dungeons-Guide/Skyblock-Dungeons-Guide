#!/usr/bin/python3
import json
import re
#
# str = ''
# with open('partymessages.txt', 'r') as f:
#     str = f.read()
#
# splitted = str.split('***************************')
#
# dict = {}
#
# mapping = [
#     'transfer_left',
#     'disband_intentional',
#     'not_in_party_1',
#     'not_in_party_2',
#     'not_in_party_3',
#     'not_in_party_4_channel',
#     'invite',
#     'someone_join',
#     'party_channel',
#     'all_invite_on',
#     'all_invite_off',
#     'invite_no_player',
#     'invite_offline_player',
#     'promote_moderator',
#     'promote_leader',
#     'demote_moderator',
#     'transfer',
#     'someone_left',
#     'disband_noone',
#     'not_in_party_5_channel_moved',
#     'self_invite',
#     [None, 'accept_invite_leader', None, 'accept_invite_members', None],
#     'invite_noperms',
#     'someone_kick',
#     'self_kick',
#     'members',
#     'self_left'
# ]
#
# currentLanguage = None
# currentDict = {}
# cnt = 0
# for lines in splitted:
#     trimmed = lines.strip()
#     if '$$LANGUAGE$$: ' in trimmed:
#         currentLanguage = trimmed.split('$$LANGUAGE$$: ')[1].strip()
#         currentDict = {}
#         dict[currentLanguage] = currentDict
#         cnt = 0
#     else:
#         print(trimmed)
#         if isinstance(mapping[cnt], list):
#             list2 = [str.strip() for str in trimmed.split('> ')[1:]]
#             for i in range(0, len(list2)):
#                 if mapping[cnt][i] is not None:
#                     currentDict[mapping[cnt][i]] = list2[i]
#
#         else:
#             currentDict[mapping[cnt]] = [str.strip() for str in trimmed.split('> ')[1:] if not (str.strip() == '§9§m-----------------------------------------------------§r' or str.strip() == '§9§m-----------------------------§r')]
#         cnt += 1

# with open('process1.json', 'w') as f:
#     json.dump(dict, f, indent=4, ensure_ascii=False)
dict = json.load(open('process1.json', 'r'))

groups = {
    'not_in_party': ['not_in_party_1', 'not_in_party_2', 'not_in_party_3', 'not_in_party_4_channel', 'not_in_party_5_channel_moved', 'disband_intentional', 'disband_noone', 'self_kick', 'self_left'],
    'party_channel': ['party_channel'],
    'all_invite_on': ['all_invite_on'],
    'all_invite_off': ['all_invite_off', 'invite_noperms'],
    'party_join': ['someone_join'],
    'party_leave': ['someone_kick', 'someone_left'],
    'invited': ['invite'],
    'invite_perm': ['invite_no_player', 'invite_offline_player'],
    'transfer': ['transfer'],
    'transfer_left': ['transfer_left'],
    'promote_leader': ['promote_leader'],
    'promote_moderator': ['promote_moderator'],
    'member': ['demote_moderator'],
    'accept_invite_leader': ['accept_invite_leader'],
    'accept_invite_members': ['accept_invite_members']
}

reverse_group = {}
for k,group_child in groups.items():
    for group in group_child:
        reverse_group[group] = k

dict2 = {}

players = ['§b[MVP§r§a+§r§b] syeyoung', '§b[MVP§r§0+§r§b] Azael_Nya', '§a[VIP] TempestBridge']


def transform(patternStr):
    occurances = [(player,x) for x,player in enumerate(players) if player in patternStr]
    if len(occurances) == 0:
        return '='+patternStr
    else:
        regex = patternStr
        for player, idx in occurances:
            regex = regex.replace(player, f'PPPPPPPPPPP{idx}')
        regex = re.escape(regex)
        for player, idx in occurances:
            regex = regex.replace(f'PPPPPPPPPPP{idx}', f'(?<p{idx}>.+)',1).replace(f'PPPPPPPPPPP{idx}', f'\\k<p{idx}>')
        return 'R'+regex
#     if any(patternStr.startsWith(player) for player in players):


for group in groups:
    dict2[group]  = []
for lang in dict.values():
    for k,v in lang.items():
        if k in reverse_group:
            patternStr = v[0] if isinstance(v, list) else v
            dict2[reverse_group[k]].append(transform(patternStr))


with open('party_languages.json', 'w') as f:
    json.dump(dict2, f, indent=4, ensure_ascii=False)